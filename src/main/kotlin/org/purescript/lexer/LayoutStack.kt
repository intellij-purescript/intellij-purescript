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

    fun insertLayout(src: SuperToken, nextPos: SourcePos) = when (src.value) {
        LOWER, TYPE -> {
            var stack = this
            val acc = mutableListOf<SuperToken>()
            while (
                stack.tail != null &&
                stack.layoutDelimiter.isIndent &&
                src.start.column < stack.sourcePos.column
            ) {
                acc += src.start.asEnd
                stack = stack.pop()
            }
            when {
                src.start.column != stack.sourcePos.column ||
                    src.start.line == stack.sourcePos.line -> Unit

                TopDecl == stack.layoutDelimiter ||
                    TopDeclHead == stack.layoutDelimiter -> {
                    stack = stack.pop()
                    acc += src.start.asSep
                }

                Of == stack.layoutDelimiter -> {
                    stack = stack.push(src.start, CaseBinders)
                    acc += src.start.asSep
                }

                stack.layoutDelimiter.isIndent -> acc += src.start.asSep
            }
            if (stack.layoutDelimiter == Property) stack = stack.pop()
            stack to acc + src
        }

        OPERATOR -> {
            var stack = this
            val acc = mutableListOf<SuperToken>()
            while (
                stack.tail != null &&
                stack.layoutDelimiter.isIndent &&
                src.start.column < stack.sourcePos.column
            ) {
                acc += src.start.asEnd
                stack = stack.pop()
            }
            when {
                src.start.column != stack.sourcePos.column ||
                    src.start.line == stack.sourcePos.line -> Unit

                TopDecl == stack.layoutDelimiter ||
                    TopDeclHead == stack.layoutDelimiter -> {
                    stack = stack.pop()
                    acc += src.start.asSep
                }

                Of == stack.layoutDelimiter -> {
                    stack = stack.push(src.start, CaseBinders)
                    acc += src.start.asSep
                }

                stack.layoutDelimiter.isIndent -> acc += src.start.asSep
            }
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
                acc += src.start.asEnd
                stack = stack.pop()
            }
            when (stack.layoutDelimiter) {
                DeclGuard -> stack.pop() to (acc + src)

                else -> {
                    var stack = this
                    val acc = mutableListOf<SuperToken>()
                    while (
                        stack.tail != null &&
                        stack.layoutDelimiter.isIndent &&
                        src.start.column < stack.sourcePos.column
                    ) {
                        acc += src.start.asEnd
                        stack = stack.pop()
                    }
                    when {
                        src.start.column != stack.sourcePos.column ||
                            src.start.line == stack.sourcePos.line -> Unit

                        TopDecl == stack.layoutDelimiter ||
                            TopDeclHead == stack.layoutDelimiter -> {
                            stack = stack.pop()
                            acc += src.start.asSep
                        }

                        Of == stack.layoutDelimiter -> {
                            stack = stack.push(src.start, CaseBinders)
                            acc += src.start.asSep
                        }

                        stack.layoutDelimiter.isIndent -> {
                            acc += src.start.asSep
                        }
                    }
                    stack to (acc + src)
                }
            }
        }

        COMMA -> {
            var stack = this
            val acc = mutableListOf<SuperToken>()
            while (stack.tail != null && stack.layoutDelimiter.isIndent) {
                acc += src.start.asEnd
                stack = stack.pop()
            }
            when (stack.layoutDelimiter) {
                Brace -> stack.push(src.start, Property)
                else -> stack
            } to (acc + src)
        }

        DOT -> {
            var stack = this
            val acc = mutableListOf<SuperToken>()
            while (
                stack.tail != null &&
                stack.layoutDelimiter.isIndent &&
                src.start.column < stack.sourcePos.column
            ) {
                acc += src.start.asEnd
                stack = stack.pop()
            }
            when {
                src.start.column != stack.sourcePos.column ||
                    src.start.line == stack.sourcePos.line -> Unit

                TopDecl == stack.layoutDelimiter ||
                    TopDeclHead == stack.layoutDelimiter -> {
                    stack = stack.pop()
                    acc += src.start.asSep
                }

                Of == stack.layoutDelimiter -> {
                    stack = stack.push(src.start, CaseBinders)
                    acc += src.start.asSep
                }

                stack.layoutDelimiter.isIndent -> {
                    acc += src.start.asSep
                }
            }
            when (stack.layoutDelimiter) {
                Forall -> stack.pop()
                else -> stack.push(src.start, Property)
            } to acc + src
        }

        ARROW -> {
            var stack =  this 
            val acc =  mutableListOf<SuperToken>() 
            while (
                stack.tail != null &&
                (stack.layoutDelimiter == Do ||
                    stack.layoutDelimiter != Of &&
                    stack.layoutDelimiter.isIndent &&
                    src.start.column <= stack.sourcePos.column
                    )
            ) {
                acc += src.start.asEnd
                stack = stack.pop()
            }
            if (stack.layoutDelimiter == CaseBinders ||
                stack.layoutDelimiter == CaseGuard ||
                stack.layoutDelimiter == LambdaBinders
            ) {
                stack = stack.pop()
            }
            stack to acc  + src
        }


        FORALL -> LayoutState(this, emptyList()).insertKwProperty(src)
        { it.pushStack(src.start, Forall) }.toPair()

        DATA -> {
            val state2 = LayoutState(this, emptyList()).insertDefault(src)
            if (state2.isTopDecl(src.start)) {
                state2.pushStack(src.start, TopDecl)
            } else {
                state2.popStack { it == Property }
            }.toPair()
        }

        CLASS -> {
            val state2 = LayoutState(this, emptyList()).insertDefault(src)
            if (state2.isTopDecl(src.start)) {
                state2.pushStack(src.start, TopDeclHead)
            } else {
                state2.popStack { it == Property }
            }.toPair()
        }

        WHERE -> when (LayoutState(
            this,
            emptyList()
        ).stack.layoutDelimiter) {
            TopDeclHead -> {
                val layoutState = LayoutState(this, emptyList())
                layoutState.copy(stack = layoutState.stack.pop())
                    .insertToken(src)
                    .insertStart(nextPos, Where)
            }

            Property -> {
                val layoutState = LayoutState(this, emptyList())
                layoutState.copy(stack = layoutState.stack.pop())
                    .insertToken(src)
            }

            else ->
                LayoutState(this, emptyList())
                    .let {
                        it.collapse(src.start) { tokPos: SourcePos, lytPos: SourcePos, lyt: LayoutDelimiter ->
                            if (lyt == Do) true
                            else lyt.isIndent && tokPos.column <= lytPos.column
                        }
                    }.insertToken(src)
                    .insertStart(
                        nextPos,
                        Where
                    )
        }.toPair()

        LPAREN -> LayoutState(this, emptyList())
            .insertDefault(src)
            .pushStack(
                src.start,
                Paren
            )
            .toPair()

        LCURLY -> LayoutState(this, emptyList())
            .insertDefault(src)
            .pushStack(
                src.start,
                Brace
            )
            .pushStack(src.start, Property).toPair()

        RPAREN -> LayoutState(this, emptyList())
            .collapse(src.start) { lyt: LayoutDelimiter -> lyt.isIndent }
            .popStack { it: LayoutDelimiter -> it == Paren }
            .insertToken(src).toPair()

        RCURLY -> LayoutState(this, emptyList())
            .collapse(src.start) { lyt: LayoutDelimiter -> lyt.isIndent }
            .popStack { it: LayoutDelimiter -> it == Property }
            .popStack { it: LayoutDelimiter -> it == Brace }
            .insertToken(src).toPair()

        LBRACK -> LayoutState(this, emptyList())
            .insertDefault(src)
            .pushStack(
                src.start,
                Square
            )
            .toPair()

        RBRACK -> LayoutState(this, emptyList())
            .collapse(src.start) { lyt: LayoutDelimiter -> lyt.isIndent }
            .popStack { it: LayoutDelimiter -> it == Square }
            .insertToken(src).toPair()

        IN -> {
            val state2 = LayoutState(
                this,
                emptyList()
            ).collapse(src.start) { lyt: LayoutDelimiter ->
                when (lyt) {
                    LayoutDelimiter.Let -> false
                    Ado -> false
                    else -> lyt.isIndent
                }
            }
            val (_, lyt, stack3) = state2.stack
            when {
                lyt == LetStmt && stack3?.layoutDelimiter == Ado -> {
                    state2.copy(stack = state2.stack.pop())
                        .insertToken(src.start.asEnd)
                        .insertToken(src.start.asEnd)
                        .insertToken(src).toPair()
                }

                lyt.isIndent -> {
                    state2.copy(stack = state2.stack.pop())
                        .insertToken(src.start.asEnd)
                        .insertToken(src).toPair()
                }

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
                    lyt == Do && p.column == src.start.column ->
                        state.insertStart(nextPos, LetStmt)

                    lyt == Ado && p.column == src.start.column ->
                        state.insertStart(nextPos, LetStmt)

                    else -> state.insertStart(nextPos, LayoutDelimiter.Let)
                }
            }

            LayoutState(this, emptyList())
                .insertKwProperty(src) { next(it) }
                .toPair()
        }


        DO ->
            LayoutState(this, emptyList()).insertKwProperty(src) {
                it.insertStart(
                    nextPos,
                    Do
                )
            }.toPair()

        ADO ->
            LayoutState(this, emptyList()).insertKwProperty(src) {
                it.insertStart(
                    nextPos,
                    Ado
                )
            }.toPair()

        CASE -> LayoutState(this, emptyList()).insertKwProperty(src)
        { it.pushStack(src.start, LayoutDelimiter.Case) }.toPair()

        OF -> {
            val state2 = LayoutState(
                this,
                emptyList()
            ).collapse(src.start) { lyt -> lyt.isIndent }
            if (state2.stack.layoutDelimiter == LayoutDelimiter.Case) {
                state2.copy(stack = state2.stack.pop())
                    .insertToken(src)
                    .insertStart(nextPos, Of)
                    .pushStack(nextPos, CaseBinders)
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
                .collapse(src.start)
                { tokPos, lytPos, lyt -> lyt.isIndent && tokPos.column <= lytPos.column }
            when (state2.stack.layoutDelimiter) {
                Of -> state2.pushStack(src.start, CaseGuard)
                    .insertToken(src)

                LayoutDelimiter.Let -> state2.pushStack(src.start, DeclGuard)
                    .insertToken(src)

                LetStmt -> state2.pushStack(src.start, DeclGuard)
                    .insertToken(src)

                Where -> state2.pushStack(src.start, DeclGuard)
                    .insertToken(src)

                else -> LayoutState(this, emptyList()).insertDefault(src)
            }.toPair()
        }

        TICK -> {
            val state2 = LayoutState(
                this,
                emptyList()
            ).collapse(src.start) { lyt -> lyt.isIndent }
            if (state2.stack.layoutDelimiter == Tick) {
                state2.copy(stack = state2.stack.pop()).insertToken(src)
            } else {
                LayoutState(this, emptyList()).insertDefault(src)
                    .pushStack(src.start, Tick)
            }.toPair()
        }

        STRING -> LayoutState(this, emptyList())
            .insertDefault(src)
            .popStack { it: LayoutDelimiter -> it == Property }
            .toPair()

        IF -> LayoutState(this, emptyList()).insertKwProperty(src) {
            it.pushStack(
                src.start,
                If
            )
        }.toPair()

        THEN -> {
            val state2 = LayoutState(
                this,
                emptyList()
            ).collapse(src.start) { lyt -> lyt.isIndent }
            if (state2.stack.layoutDelimiter == If) {
                state2.copy(stack = state2.stack.pop())
                    .insertToken(src)
                    .pushStack(src.start, Then)
            } else {
                LayoutState(this, emptyList())
                    .insertDefault(src)
                    .popStack { it: LayoutDelimiter -> it == Property }
            }.toPair()
        }

        ELSE -> {
            val state2 = LayoutState(
                this,
                emptyList()
            ).collapse(src.start) { lyt -> lyt.isIndent }
            if (state2.stack.layoutDelimiter == Then) {
                state2.copy(stack = state2.stack.pop()).insertToken(src)
            } else {
                val state3 = LayoutState(this, emptyList()).collapse(
                    src.start
                ) { tokPos, lytPos, lyt -> lyt.isIndent && tokPos.column < lytPos.column }
                if (state3.isTopDecl(src.start)) {
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
}