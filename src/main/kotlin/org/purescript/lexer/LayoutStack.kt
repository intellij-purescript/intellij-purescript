package org.purescript.lexer

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

    fun insertLayout(src: SuperToken, nextPos: SourcePos): LayoutState {
        val state = LayoutState(this, emptyList())
        return when (src.value) {
            LOWER, TYPE -> state
                .insertDefault(src)
                .popStack { it == LayoutDelimiter.Property }

            OPERATOR -> state
                .collapse(src.start, ::offsideP)
                .insertSep(src.start)
                .insertToken(src)

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
            }

            COMMA -> state.collapse(src.start) { it -> it.isIndent }.let {
                when (it.stack.layoutDelimiter) {
                    LayoutDelimiter.Brace -> it.insertToken(src)
                        .pushStack(src.start, LayoutDelimiter.Property)

                    else -> it.insertToken(src)
                }
            }

            DOT -> state.insertDefault(src).let {
                when (it.stack.layoutDelimiter) {
                    LayoutDelimiter.Forall -> it.popStack()
                    else -> it.pushStack(src.start, LayoutDelimiter.Property)
                }
            }

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
                }.insertToken(src)


            FORALL -> state.insertKwProperty(src)
            { it.pushStack(src.start, LayoutDelimiter.Forall) }

            DATA -> {
                val state2 = state.insertDefault(src)
                if (state2.isTopDecl(src.start)) {
                    state2.pushStack(src.start, LayoutDelimiter.TopDecl)
                } else {
                    state2.popStack { it == LayoutDelimiter.Property }
                }
            }

            CLASS -> {
                val state2 = state.insertDefault(src)
                if (state2.isTopDecl(src.start)) {
                    state2.pushStack(src.start, LayoutDelimiter.TopDeclHead)
                } else {
                    state2.popStack { it == LayoutDelimiter.Property }
                }
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
            }

            LPAREN -> state.insertDefault(src).pushStack(
                src.start,
                LayoutDelimiter.Paren
            )

            LCURLY -> state.insertDefault(src).pushStack(
                src.start,
                LayoutDelimiter.Brace
            )
                .pushStack(src.start, LayoutDelimiter.Property)

            RPAREN -> state
                .collapse(src.start) { lyt: LayoutDelimiter -> lyt.isIndent }
                .popStack { it: LayoutDelimiter -> it == LayoutDelimiter.Paren }
                .insertToken(src)

            RCURLY -> state
                .collapse(src.start) { lyt: LayoutDelimiter -> lyt.isIndent }
                .popStack { it: LayoutDelimiter -> it == LayoutDelimiter.Property }
                .popStack { it: LayoutDelimiter -> it == LayoutDelimiter.Brace }
                .insertToken(src)

            LBRACK -> state.insertDefault(src).pushStack(
                src.start,
                LayoutDelimiter.Square
            )

            RBRACK -> state
                .collapse(src.start) { lyt: LayoutDelimiter -> lyt.isIndent }
                .popStack { it: LayoutDelimiter -> it == LayoutDelimiter.Square }
                .insertToken(src)

            IN -> {
                val state2 = state.collapse(src.start) { lyt: LayoutDelimiter ->
                    when (lyt) {
                        LayoutDelimiter.Let -> false
                        LayoutDelimiter.Ado -> false
                        else -> lyt.isIndent
                    }
                }
                val (_, lyt, stack3) = state2.stack
                if (lyt == LayoutDelimiter.LetStmt && stack3?.layoutDelimiter == LayoutDelimiter.Ado) {
                    return state2.popStack()
                        .insertToken(src.start.asEnd)
                        .insertToken(src.start.asEnd)
                        .insertToken(src)
                } else if (lyt.isIndent) {
                    return state2.popStack()
                        .insertToken(src.start.asEnd)
                        .insertToken(src)
                } else {
                    return state.insertDefault(src)
                        .popStack { it: LayoutDelimiter -> it == LayoutDelimiter.Property }
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

                return state.insertKwProperty(src) { next(it) }
            }

            DO ->
                state.insertKwProperty(src) {
                    it.insertStart(
                        nextPos,
                        LayoutDelimiter.Do
                    )
                }

            ADO ->
                state.insertKwProperty(src) {
                    it.insertStart(
                        nextPos,
                        LayoutDelimiter.Ado
                    )
                }

            CASE -> state.insertKwProperty(src)
            { it.pushStack(src.start, LayoutDelimiter.Case) }

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
                }
            }

            BACKSLASH -> state
                .insertDefault(src)
                .pushStack(src.start, LayoutDelimiter.LambdaBinders)

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
                }
            }

            TICK -> {
                val state2 = state.collapse(src.start) { lyt -> lyt.isIndent }
                if (state2.stack.layoutDelimiter == LayoutDelimiter.Tick) {
                    state2.popStack().insertToken(src)
                } else {
                    state.insertDefault(src)
                        .pushStack(src.start, LayoutDelimiter.Tick)
                }
            }

            STRING -> state
                .insertDefault(src)
                .popStack { it: LayoutDelimiter -> it == LayoutDelimiter.Property }

            IF -> state.insertKwProperty(src) {
                it.pushStack(
                    src.start,
                    LayoutDelimiter.If
                )
            }

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
                }
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
                }
            }

            else -> state.insertDefault(src)
        }
    }
}