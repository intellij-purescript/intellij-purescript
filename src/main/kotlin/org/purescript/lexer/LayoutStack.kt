package org.purescript.lexer

import org.purescript.lexer.LayoutDelimiter.*
import org.purescript.lexer.token.SourcePos
import org.purescript.parser.*

data class LayoutStack(
    val sourcePos: SourcePos,
    val layoutDelimiter: LayoutDelimiter,
    val tail: LayoutStack?
) {
    val column get() = sourcePos.column
    val line get() = sourcePos.line
    val endsByDedent get() = layoutDelimiter.endsByDedent
    fun isTopDecl(tokPos: SourcePos): Boolean = when {
        tail == null || tail.tail != null -> false
        tail.layoutDelimiter != Root -> false
        layoutDelimiter != Where -> false
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

    fun pop() = tail ?: this

    fun insertLayout(src: SuperToken) = when (src.value) {
        LOWER, TYPE, FALSE, TRUE, IMPORT -> {
            var (stack, acc) = collapse(src)
            if (stack.layoutDelimiter == Property) stack = stack.pop()
            stack to acc + src
        }

        OPERATOR -> {
            val (stack, acc) = collapse(src)
            stack to acc + src
        }

        EQ -> {
            var stack = this
            val acc = mutableListOf<SuperToken>()
            while (
                stack.tail != null &&
                (
                        stack.layoutDelimiter == Where ||
                                stack.layoutDelimiter == LayoutDelimiter.Let ||
                                stack.layoutDelimiter == LetStmt
                        )
            ) {
                acc += src.asEnd
                stack = stack.pop()
            }
            when (stack.layoutDelimiter) {
                DeclGuard -> stack.pop() to (acc + src)

                else -> {
                    val (stack, acc) = collapse(src)
                    stack to (acc + src)
                }
            }
        }

        COMMA -> {
            var stack = this
            val acc = mutableListOf<SuperToken>()
            while (stack.tail != null && stack.endsByDedent) {
                acc += src.asEnd
                stack = stack.pop()
            }
            when (stack.layoutDelimiter) {
                Brace -> stack.push(src.start, Property)
                else -> stack
            } to (acc + src)
        }

        DOT -> {
            val (stack, acc) = collapse(src)
            when (stack.layoutDelimiter) {
                Forall -> stack.pop()
                else -> stack.push(src.start, Property)
            } to acc + src
        }

        ARROW -> {
            var stack = this
            val acc = mutableListOf<SuperToken>()
            while (
                stack.tail != null &&
                (stack.layoutDelimiter == Do ||
                        stack.layoutDelimiter == If /* relax "If" if is started but missing then*/ ||
                        stack.layoutDelimiter == Then /* relax "If" if is started but missing else*/ ||
                        stack.layoutDelimiter != Of &&
                        stack.endsByDedent &&
                        src.column <= stack.sourcePos.column
                        )
            ) {
                acc += src.asEnd
                stack = stack.pop()
            }
            if (stack.layoutDelimiter == CaseBinders ||
                stack.layoutDelimiter == CaseGuard ||
                stack.layoutDelimiter == LambdaBinders
            ) {
                stack = stack.pop()
            }
            stack to acc + src
        }


        FORALL -> {
            val (stack, acc) = collapse(src)
            when (stack.layoutDelimiter) {
                Property -> stack.pop()
                else -> stack.push(src.start, Forall)
            } to acc + src
        }

        DATA -> {
            val (stack, acc) = collapse(src)
            when {
                stack.isTopDecl(src.start) -> stack.push(src.start, TopDecl)
                stack.layoutDelimiter == Property -> stack.pop()
                else -> stack
            } to acc + src
        }

        CLASS -> {
            val state2 = LayoutState(this, emptyList()).insertDefault(src)
            if (state2.stack.isTopDecl(src.start)) {
                state2.pushStack(src.start, TopDeclHead)
            } else {
                state2.popStack { it == Property }
            }.toPair()
        }

        WHERE -> when (this.layoutDelimiter) {
            TopDeclHead -> LayoutState(pop(), emptyList())
                .insertToken(src)
                .insertStart(src, Where)

            Property -> LayoutState(pop(), emptyList()).insertToken(src)
            else -> LayoutState(this, emptyList())
                .collapse(src) { token, lytPos, lyt ->
                    lyt == Do || lyt.endsByDedent && token.column <= lytPos.column
                }
                .insertToken(src)
                .insertStart(src, Where)
        }.toPair()

        LPAREN -> LayoutState(this, emptyList())
            .insertDefault(src)
            .pushStack(src.start, Paren)
            .toPair()

        LCURLY -> LayoutState(this, emptyList())
            .insertDefault(src)
            .pushStack(src.start, Brace)
            .pushStack(src.start, Property).toPair()

        RPAREN -> LayoutState(this, emptyList())
            .collapse(src) { lyt: LayoutDelimiter -> lyt.endsByDedent }
            .popStack { it: LayoutDelimiter -> it == Paren }
            .insertToken(src).toPair()

        RCURLY -> LayoutState(this, emptyList())
            .collapse(src) { lyt: LayoutDelimiter -> lyt.endsByDedent }
            .popStack { it: LayoutDelimiter -> it == Property }
            .popStack { it: LayoutDelimiter -> it == Brace }
            .insertToken(src).toPair()

        LBRACK -> LayoutState(this, emptyList())
            .insertDefault(src)
            .pushStack(src.start, Square)
            .toPair()

        RBRACK -> LayoutState(this, emptyList())
            .collapse(src) { lyt: LayoutDelimiter -> lyt.endsByDedent }
            .popStack { it: LayoutDelimiter -> it == Square }
            .insertToken(src).toPair()

        IN -> {
            val state2 = LayoutState(this, emptyList())
                .collapse(src) { lyt: LayoutDelimiter ->
                    when (lyt) {
                        LayoutDelimiter.Let -> false
                        Ado -> false
                        else -> lyt.endsByDedent
                    }
                }
            val (_, lyt, stack3) = state2.stack
            when {
                lyt == LetStmt && stack3?.layoutDelimiter == Ado -> {
                    state2.popStack()
                        .insertToken(src.asEnd)
                        .insertToken(src.asEnd)
                        .insertToken(src).toPair()
                }

                lyt.endsByDedent -> state2.popStack()
                    .insertToken(src.asEnd)
                    .insertToken(src).toPair()

                else -> {
                    LayoutState(this, emptyList())
                        .insertDefault(src)
                        .popStack { it: LayoutDelimiter -> it == Property }
                        .toPair()
                }
            }
        }

        LET -> {
            fun next(state: LayoutState): LayoutState {
                val (p, lyt, _) = state.stack
                return when {
                    lyt == Do && p.column == src.column -> state.insertStart(src, LetStmt)
                    lyt == Ado && p.column == src.column -> state.insertStart(src, LetStmt)
                    else -> state.insertStart(src, LayoutDelimiter.Let)
                }
            }

            LayoutState(this, emptyList())
                .insertKwProperty(src) { next(it) }
                .toPair()
        }

        DO ->
            LayoutState(this, emptyList())
                .insertKwProperty(src) { it.insertStart(src, Do) }
                .toPair()

        ADO ->
            LayoutState(this, emptyList())
                .insertKwProperty(src) { it.insertStart(src, Ado) }
                .toPair()

        CASE -> LayoutState(this, emptyList()).insertKwProperty(src)
        { it.pushStack(src.start, LayoutDelimiter.Case) }.toPair()

        OF -> {
            val state2 = LayoutState(this, emptyList())
                .collapse(src) { lyt -> lyt.endsByDedent }
            if (state2.stack.layoutDelimiter == LayoutDelimiter.Case) {
                state2.popStack()
                    .insertToken(src)
                    .insertStart(src, Of)
                    .pushStack(src.end, CaseBinders)
            } else {
                state2
                    .insertDefault(src)
                    .popStack { it: LayoutDelimiter -> it == Property }
            }.toPair()
        }


        BACKSLASH -> LayoutState(this, emptyList())
            .insertDefault(src)
            .pushStack(src.start, LambdaBinders).toPair()

        PIPE -> {
            val state2 = LayoutState(this, emptyList())
                .collapse(src)
                { tokPos, lytPos, lyt -> lyt.endsByDedent && tokPos.column <= lytPos.column }
            when (state2.stack.layoutDelimiter) {
                Of -> state2.pushStack(src.start, CaseGuard).insertToken(src)
                LayoutDelimiter.Let -> state2.pushStack(src.start, DeclGuard).insertToken(src)
                LetStmt -> state2.pushStack(src.start, DeclGuard).insertToken(src)
                Where -> state2.pushStack(src.start, DeclGuard).insertToken(src)
                else -> LayoutState(this, emptyList()).insertDefault(src)
            }.toPair()
        }

        TICK -> {
            val state2 = LayoutState(this, emptyList())
                .collapse(src) { lyt -> lyt.endsByDedent }
            if (state2.stack.layoutDelimiter == Tick) {
                state2.popStack().insertToken(src)
            } else {
                LayoutState(this, emptyList()).insertDefault(src).pushStack(src.start, Tick)
            }.toPair()
        }

        STRING -> LayoutState(this, emptyList())
            .insertDefault(src)
            .popStack { it: LayoutDelimiter -> it == Property }
            .toPair()

        IF -> LayoutState(this, emptyList())
            .insertKwProperty(src) { it.pushStack(src.start, If) }
            .toPair()

        THEN -> {
            val state2 = LayoutState(this, emptyList())
                .collapse(src) { lyt -> lyt.endsByDedent }
            if (state2.stack.layoutDelimiter == If) {
                state2.popStack()
                    .insertToken(src)
                    .pushStack(src.start, Then)
            } else {
                LayoutState(this, emptyList())
                    .insertDefault(src)
                    .popStack { it: LayoutDelimiter -> it == Property }
            }.toPair()
        }

        ELSE -> {
            val state2 = LayoutState(this, emptyList())
                .collapse(src) { lyt -> lyt.endsByDedent }
            if (state2.stack.layoutDelimiter == Then) {
                state2.popStack().insertToken(src)
            } else {
                val state3 = LayoutState(this, emptyList())
                    .collapse(src) { tokPos, lytPos, lyt -> lyt.endsByDedent && tokPos.column < lytPos.column }
                if (state3.stack.isTopDecl(src.start)) {
                    state3.insertToken(src)
                } else {
                    state3
                        .insertSep(src.start)
                        .insertToken(src)
                        .popStack { it == Property }
                }
            }.toPair()
        }

        else -> LayoutState(this, emptyList()).insertDefault(src).toPair()
    }

    private fun collapse(src: SuperToken): Pair<LayoutStack, MutableList<SuperToken>> {
        var stack = this
        val acc = mutableListOf<SuperToken>()
        while (
            stack.tail != null &&
            stack.endsByDedent &&
            src.column < stack.column
        ) {
            stack = stack.pop()
            acc += src.asEnd
        }
        when {
            src.column != stack.column || src.line == stack.line -> Unit
            TopDecl == stack.layoutDelimiter || TopDeclHead == stack.layoutDelimiter -> {
                stack = stack.pop()
                acc += src.asSep
            }

            Of == stack.layoutDelimiter -> {
                stack = stack.push(src.start, CaseBinders)
                acc += src.asSep
            }

            stack.endsByDedent -> acc += src.asSep
        }
        return stack to acc
    }
}