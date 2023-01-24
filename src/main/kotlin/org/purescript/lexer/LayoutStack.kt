package org.purescript.lexer

import org.purescript.lexer.LayoutDelimiter.*
import org.purescript.lexer.token.SourcePos
import org.purescript.parser.*

data class LayoutStack(
    val sourcePos: SourcePos,
    val layoutDelimiter: LayoutDelimiter,
    val tail: LayoutStack?
) {
    fun isTopDecl(tokPos: SourcePos): Boolean = when {
        tail == null || tail.tail != null -> false
        tail.layoutDelimiter != LayoutDelimiter.Root -> false
        layoutDelimiter != LayoutDelimiter.Where -> false
        else -> tokPos.column == sourcePos.column
    }

    inline fun count(filter: (LayoutDelimiter) -> Boolean): Int {
        var tail = this.tail
        var count = if (filter(layoutDelimiter)) 1 else 0
        while (tail != null) {
            if (filter(tail.layoutDelimiter)) count++
            tail = tail.tail
        }
        return count
    }

    inline fun find(filter: (LayoutDelimiter) -> Boolean): LayoutStack? {
        var stack: LayoutStack? = this
        while (stack != null) {
            if (filter(stack.layoutDelimiter)) return stack
            stack = stack.tail
        }
        return null
    }

    fun push(sourcePos: SourcePos, layoutDelimiter: LayoutDelimiter) =
        LayoutStack(sourcePos, layoutDelimiter, this)

    inline fun pop(): LayoutStack = pop { true }
    inline fun pop(p: (LayoutDelimiter) -> Boolean): LayoutStack = when {
        tail == null || !p(layoutDelimiter) -> this
        else -> tail
    }

    fun insertLayout(
        src: SuperToken,
        nextPos: SourcePos
    ): Pair<LayoutStack, List<SuperToken>> {
        val state = LayoutState(this, emptyList())
        return when (src.value) {
            LOWER, TYPE -> {
                var stack = this
                var acc = emptyList<SuperToken>()
                while (
                    stack.tail != null &&
                    stack.layoutDelimiter.isIndent &&
                    src.start.column < stack.sourcePos.column
                ) {
                    if (stack.layoutDelimiter.isIndent) {
                        acc = acc + src.start.asEnd
                    }
                    stack = stack.pop()
                }
                when {
                    src.start.column != stack.sourcePos.column ||
                        src.start.line == stack.sourcePos.line -> {
                    }

                    TopDecl == stack.layoutDelimiter ||
                        TopDeclHead == stack.layoutDelimiter -> {
                        stack = stack.pop()
                        acc = acc + src.start.asSep
                    }

                    Of == stack.layoutDelimiter -> {
                        acc = acc + src.start.asSep 
                        stack = stack.push(src.start, CaseBinders)
                    }

                    stack.layoutDelimiter.isIndent ->
                        acc = acc + src.start.asSep
                }
                if (stack.layoutDelimiter == Property) stack = stack.pop()
                stack to acc + src
            }

            OPERATOR -> state
                .collapse(src.start, ::offsideP)
                .insertSep(src.start)
                .insertToken(src).toPair()

            EQ -> state.collapse(src.start) { lyt ->
                when (lyt) {
                    LayoutDelimiter.Where -> true
                    LayoutDelimiter.Let -> true
                    LayoutDelimiter.LetStmt -> true
                    else -> false
                }
            }.let {
                when (it.stack.layoutDelimiter) {
                    LayoutDelimiter.DeclGuard -> it.popStack().insertToken(src)
                    else -> state.insertDefault(src)
                }
            }.toPair()

            COMMA -> state.collapse(src.start) { it -> it.isIndent }.let {
                when (it.stack.layoutDelimiter) {
                    LayoutDelimiter.Brace -> it.insertToken(src)
                        .pushStack(src.start, LayoutDelimiter.Property)

                    else -> it.insertToken(src)
                }
            }.toPair()

            DOT -> state.insertDefault(src).let {
                when (it.stack.layoutDelimiter) {
                    LayoutDelimiter.Forall -> it.popStack()
                    else -> it.pushStack(src.start, LayoutDelimiter.Property)
                }
            }.toPair()

            ARROW -> state
                .collapse(src.start) { tokPos, lytPos, lyt ->
                    when (lyt) {
                        LayoutDelimiter.Do -> true
                        LayoutDelimiter.Of -> false
                        else -> lyt.isIndent && tokPos.column <= lytPos.column
                    }
                }.popStack {
                    when (it) {
                        LayoutDelimiter.CaseBinders -> true
                        LayoutDelimiter.CaseGuard -> true
                        LayoutDelimiter.LambdaBinders -> true
                        else -> false
                    }
                }.insertToken(src).toPair()


            FORALL -> state.insertKwProperty(src)
            { it.pushStack(src.start, LayoutDelimiter.Forall) }.toPair()

            DATA -> {
                val state2 = state.insertDefault(src)
                if (state2.isTopDecl(src.start)) {
                    state2.pushStack(src.start, LayoutDelimiter.TopDecl)
                } else {
                    state2.popStack { it == LayoutDelimiter.Property }
                }.toPair()
            }

            CLASS -> {
                val state2 = state.insertDefault(src)
                if (state2.isTopDecl(src.start)) {
                    state2.pushStack(src.start, LayoutDelimiter.TopDeclHead)
                } else {
                    state2.popStack { it == LayoutDelimiter.Property }
                }.toPair()
            }

            WHERE -> when (state.stack.layoutDelimiter) {
                LayoutDelimiter.TopDeclHead -> state.popStack().insertToken(src)
                    .insertStart(nextPos, LayoutDelimiter.Where)

                LayoutDelimiter.Property -> state.popStack().insertToken(src)

                else ->
                    state
                        .let {
                            it.collapse(src.start) { tokPos: SourcePos, lytPos: SourcePos, lyt: LayoutDelimiter ->
                                if (lyt == LayoutDelimiter.Do) true
                                else lyt.isIndent && tokPos.column <= lytPos.column
                            }
                        }.insertToken(src)
                        .insertStart(
                            nextPos,
                            LayoutDelimiter.Where
                        )
            }.toPair()

            LPAREN -> state.insertDefault(src).pushStack(
                src.start,
                LayoutDelimiter.Paren
            ).toPair()

            LCURLY -> state.insertDefault(src).pushStack(
                src.start,
                LayoutDelimiter.Brace
            )
                .pushStack(src.start, LayoutDelimiter.Property).toPair()

            RPAREN -> state
                .collapse(src.start) { lyt: LayoutDelimiter -> lyt.isIndent }
                .popStack { it: LayoutDelimiter -> it == LayoutDelimiter.Paren }
                .insertToken(src).toPair()

            RCURLY -> state
                .collapse(src.start) { lyt: LayoutDelimiter -> lyt.isIndent }
                .popStack { it: LayoutDelimiter -> it == LayoutDelimiter.Property }
                .popStack { it: LayoutDelimiter -> it == LayoutDelimiter.Brace }
                .insertToken(src).toPair()

            LBRACK -> state.insertDefault(src).pushStack(
                src.start,
                LayoutDelimiter.Square
            ).toPair()

            RBRACK -> state
                .collapse(src.start) { lyt: LayoutDelimiter -> lyt.isIndent }
                .popStack { it: LayoutDelimiter -> it == LayoutDelimiter.Square }
                .insertToken(src).toPair()

            IN -> {
                val state2 = state.collapse(src.start) { lyt: LayoutDelimiter ->
                    when (lyt) {
                        LayoutDelimiter.Let -> false
                        LayoutDelimiter.Ado -> false
                        else -> lyt.isIndent
                    }
                }
                val (_, lyt, stack3) = state2.stack
                return when {
                    lyt == LayoutDelimiter.LetStmt && stack3?.layoutDelimiter == LayoutDelimiter.Ado -> {
                        state2.popStack()
                            .insertToken(src.start.asEnd)
                            .insertToken(src.start.asEnd)
                            .insertToken(src).toPair()
                    }

                    lyt.isIndent -> {
                        state2.popStack()
                            .insertToken(src.start.asEnd)
                            .insertToken(src).toPair()
                    }

                    else -> {
                        state
                            .insertDefault(src)
                            .popStack { it: LayoutDelimiter -> it == LayoutDelimiter.Property }
                            .toPair()
                    }
                }
            }

            LET -> {
                fun next(state: LayoutState): LayoutState {
                    val (p, lyt, _) = state.stack
                    return when {
                        lyt == LayoutDelimiter.Do && p.column == src.start.column ->
                            state.insertStart(nextPos, LayoutDelimiter.LetStmt)

                        lyt == LayoutDelimiter.Ado && p.column == src.start.column ->
                            state.insertStart(nextPos, LayoutDelimiter.LetStmt)

                        else -> state.insertStart(nextPos, LayoutDelimiter.Let)
                    }
                }

                return state.insertKwProperty(src) { next(it) }.toPair()
            }

                

            DO ->
                state.insertKwProperty(src) {
                    it.insertStart(
                        nextPos,
                        LayoutDelimiter.Do
                    )
                }.toPair()

            ADO ->
                state.insertKwProperty(src) {
                    it.insertStart(
                        nextPos,
                        LayoutDelimiter.Ado
                    )
                }.toPair()

            CASE -> state.insertKwProperty(src)
            { it.pushStack(src.start, LayoutDelimiter.Case) }.toPair()

            OF -> {
                val state2 = state.collapse(src.start) { lyt -> lyt.isIndent }
                return if (state2.stack.layoutDelimiter == LayoutDelimiter.Case) {
                    state2.popStack()
                        .insertToken(src)
                        .insertStart(nextPos, LayoutDelimiter.Of)
                        .pushStack(nextPos, LayoutDelimiter.CaseBinders)
                } else {
                    state2
                        .insertDefault(src)
                        .popStack { it: LayoutDelimiter -> it == LayoutDelimiter.Property }
                }.toPair()
            }


            BACKSLASH -> state
                .insertDefault(src)
                .pushStack(src.start, LayoutDelimiter.LambdaBinders).toPair()

            PIPE -> {
                val state2 =
                    state.collapse(src.start) { tokPos, lytPos, lyt -> lyt.isIndent && tokPos.column <= lytPos.column }
                when (state2.stack.layoutDelimiter) {
                    LayoutDelimiter.Of -> state2.pushStack(
                        src.start,
                        LayoutDelimiter.CaseGuard
                    ).insertToken(src)

                    LayoutDelimiter.Let -> state2
                        .pushStack(src.start, LayoutDelimiter.DeclGuard)
                        .insertToken(src)

                    LayoutDelimiter.LetStmt -> state2.pushStack(
                        src.start,
                        LayoutDelimiter.DeclGuard
                    ).insertToken(src)

                    LayoutDelimiter.Where -> state2.pushStack(
                        src.start,
                        LayoutDelimiter.DeclGuard
                    ).insertToken(src)

                    else -> state.insertDefault(src)
                }.toPair()
            }

            TICK -> {
                val state2 = state.collapse(src.start) { lyt -> lyt.isIndent }
                if (state2.stack.layoutDelimiter == LayoutDelimiter.Tick) {
                    state2.popStack().insertToken(src)
                } else {
                    state.insertDefault(src)
                        .pushStack(src.start, LayoutDelimiter.Tick)
                }.toPair()
            }

            STRING -> state
                .insertDefault(src)
                .popStack { it: LayoutDelimiter -> it == LayoutDelimiter.Property }
                .toPair()

            IF -> state.insertKwProperty(src) {
                it.pushStack(
                    src.start,
                    LayoutDelimiter.If
                )
            }.toPair()

            THEN -> {
                val state2 = state.collapse(src.start) { lyt -> lyt.isIndent }
                if (state2.stack.layoutDelimiter == LayoutDelimiter.If) {
                    state2.popStack()
                        .insertToken(src)
                        .pushStack(src.start, LayoutDelimiter.Then)
                } else {
                    state
                        .insertDefault(src)
                        .popStack { it: LayoutDelimiter -> it == LayoutDelimiter.Property }
                }.toPair()
            }

            ELSE -> {
                val state2 = state.collapse(src.start) { lyt -> lyt.isIndent }
                if (state2.stack.layoutDelimiter == LayoutDelimiter.Then) {
                    state2.popStack().insertToken(src)
                } else {
                    val state3 = state.collapse(src.start, ::offsideP)
                    if (state3.isTopDecl(src.start)) {
                        state3.insertToken(src)
                    } else {
                        state3
                            .insertSep(src.start)
                            .insertToken(src)
                            .popStack { it == LayoutDelimiter.Property }
                    }
                }.toPair()
            }

            else -> state.insertDefault(src).toPair()
        }
    }
}