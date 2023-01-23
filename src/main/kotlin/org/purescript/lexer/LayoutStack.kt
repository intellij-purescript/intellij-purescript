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
    fun insertLayout(src: SuperToken, nextPos: SourcePos): LayoutState {
        val tokenValue = src.value
        val tokPos = src.start
        val state = LayoutState(this, emptyList())
        val (stk, acc) = state

        return when (tokenValue) {
            LOWER, TYPE -> state
                .insertDefault(src, tokPos)
                .popStack { it == LayoutDelimiter.Property }

            OPERATOR -> state
                .collapse(tokPos, ::offsideP)
                .insertSep(tokPos)
                .insertToken(src)

            EQ -> state.collapse(tokPos) { lyt ->
                when (lyt) {
                    LayoutDelimiter.Where -> true
                    LayoutDelimiter.Let -> true
                    LayoutDelimiter.LetStmt -> true
                    else -> false
                }
            }.let {
                when (it.stack?.layoutDelimiter) {
                    LayoutDelimiter.DeclGuard -> it.popStack().insertToken(src)
                    else -> state.insertDefault(src, tokPos)
                }
            }

            COMMA -> state.collapse(tokPos) { it -> it.isIndent }.let {
                when (it.stack?.layoutDelimiter) {
                    LayoutDelimiter.Brace -> it.insertToken(src).pushStack(tokPos,
                        LayoutDelimiter.Property
                    )
                    else -> it.insertToken(src)
                }
            }

            DOT -> state.insertDefault(src, tokPos).let {
                when (it.stack?.layoutDelimiter) {
                    LayoutDelimiter.Forall -> it.popStack()
                    else -> it.pushStack(tokPos, LayoutDelimiter.Property)
                }
            }

            ARROW -> state
                .collapse(tokPos) { tokPos, lytPos, lyt ->
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


            FORALL -> state.insertKwProperty(src, tokPos)
            { it.pushStack(tokPos, LayoutDelimiter.Forall) }

            DATA -> {
                val state2 = state.insertDefault(src, tokPos)
                if (state2.isTopDecl(tokPos)) {
                    state2.pushStack(tokPos, LayoutDelimiter.TopDecl)
                } else {
                    state2.popStack { it == LayoutDelimiter.Property }
                }
            }

            CLASS -> {
                val state2 = state.insertDefault(src, tokPos)
                if (state2.isTopDecl(tokPos)) {
                    state2.pushStack(tokPos, LayoutDelimiter.TopDeclHead)
                } else {
                    state2.popStack { it == LayoutDelimiter.Property }
                }
            }

            WHERE -> when (stk?.layoutDelimiter) {
                LayoutDelimiter.TopDeclHead -> LayoutState(stk.tail, acc).insertToken(src)
                    .insertStart(nextPos, LayoutDelimiter.Where)

                LayoutDelimiter.Property -> LayoutState(stk.tail, acc).insertToken(src)

                else ->
                    state
                        .let {
                            it.collapse(tokPos) { tokPos: SourcePos, lytPos: SourcePos, lyt: LayoutDelimiter ->
                                if (lyt == LayoutDelimiter.Do) true
                                else lyt.isIndent && tokPos.column <= lytPos.column
                            }
                        }.insertToken(src)
                        .insertStart(
                            nextPos,
                            LayoutDelimiter.Where
                        )
            }

            LPAREN -> state.insertDefault(src, tokPos).pushStack(tokPos,
                LayoutDelimiter.Paren
            )
            LCURLY -> state.insertDefault(src, tokPos).pushStack(tokPos,
                LayoutDelimiter.Brace
            )
                .pushStack(tokPos, LayoutDelimiter.Property)

            RPAREN -> state
                .collapse(tokPos) { lyt: LayoutDelimiter -> lyt.isIndent }
                .popStack { it: LayoutDelimiter -> it == LayoutDelimiter.Paren }
                .insertToken(src)

            RCURLY -> state
                .collapse(tokPos) { lyt: LayoutDelimiter -> lyt.isIndent }
                .popStack { it: LayoutDelimiter -> it == LayoutDelimiter.Property }
                .popStack { it: LayoutDelimiter -> it == LayoutDelimiter.Brace }
                .insertToken(src)

            LBRACK -> state.insertDefault(src, tokPos).pushStack(tokPos,
                LayoutDelimiter.Square
            )
            RBRACK -> state
                .collapse(tokPos) { lyt: LayoutDelimiter -> lyt.isIndent }
                .popStack { it: LayoutDelimiter -> it == LayoutDelimiter.Square }
                .insertToken(src)

            IN -> {
                val (stk1, acc2) = state.collapse(tokPos) { lyt: LayoutDelimiter ->
                    when (lyt) {
                        LayoutDelimiter.Let -> false
                        LayoutDelimiter.Ado -> false
                        else -> lyt.isIndent
                    }
                }
                val (_, lyt, stk2) = stk1 ?: return state.insertDefault(src, tokPos)
                    .let { state1 -> state1.popStack { it: LayoutDelimiter -> it == LayoutDelimiter.Property } }
                if (lyt == LayoutDelimiter.LetStmt && stk2?.layoutDelimiter == LayoutDelimiter.Ado) {
                    return LayoutState(stk2.tail, acc2)
                        .insertToken(tokPos.asEnd)
                        .insertToken(tokPos.asEnd)
                        .insertToken(src)
                } else if (lyt.isIndent) {
                    return LayoutState(stk2, acc2)
                        .insertToken(tokPos.asEnd)
                        .insertToken(src)
                } else {
                    return state.insertDefault(src, tokPos)
                        .popStack { it: LayoutDelimiter -> it == LayoutDelimiter.Property }
                }
            }

            LET -> {
                fun next(state: LayoutState): LayoutState {
                    val (p, lyt, _) = state.stack ?: return state
                        .insertStart(
                            nextPos,
                            LayoutDelimiter.Let
                        )
                    return when {
                        lyt == LayoutDelimiter.Do && p.column == tokPos.column ->
                            state.insertStart(nextPos, LayoutDelimiter.LetStmt)

                        lyt == LayoutDelimiter.Ado && p.column == tokPos.column ->
                            state.insertStart(nextPos, LayoutDelimiter.LetStmt)

                        else -> state.insertStart(nextPos, LayoutDelimiter.Let)
                    }
                }

                return state.insertKwProperty(src, tokPos) { next(it) }
            }

            DO ->
                state.insertKwProperty(src, tokPos) { it.insertStart(nextPos,
                    LayoutDelimiter.Do
                ) }

            ADO ->
                state.insertKwProperty(src, tokPos) { it.insertStart(nextPos,
                    LayoutDelimiter.Ado
                ) }

            CASE -> state.insertKwProperty(src, tokPos)
            { it.pushStack(tokPos, LayoutDelimiter.Case) }
            OF -> {
                val state2 = state.collapse(tokPos) { lyt-> lyt.isIndent }
                return if (state2.stack?.layoutDelimiter == LayoutDelimiter.Case) {
                    LayoutState(state2.stack.tail, state2.acc)
                        .insertToken(src)
                        .insertStart(nextPos, LayoutDelimiter.Of)
                        .pushStack(nextPos, LayoutDelimiter.CaseBinders)
                } else {
                    state2
                        .insertDefault(src, tokPos)
                        .popStack { it: LayoutDelimiter -> it == LayoutDelimiter.Property }
                }
            }

            BACKSLASH -> state
                .insertDefault(src, tokPos)
                .pushStack(tokPos, LayoutDelimiter.LambdaBinders)

            PIPE -> {
                val state2 =
                    state.collapse(tokPos) { tokPos, lytPos, lyt -> lyt.isIndent && tokPos.column <= lytPos.column }
                when (state2.stack?.layoutDelimiter) {
                    LayoutDelimiter.Of -> state2.pushStack(tokPos,
                        LayoutDelimiter.CaseGuard
                    ).insertToken(src)
                    LayoutDelimiter.Let -> state2
                        .pushStack(tokPos, LayoutDelimiter.DeclGuard)
                        .insertToken(src)

                    LayoutDelimiter.LetStmt -> state2.pushStack(tokPos,
                        LayoutDelimiter.DeclGuard
                    ).insertToken(src)
                    LayoutDelimiter.Where -> state2.pushStack(tokPos,
                        LayoutDelimiter.DeclGuard
                    ).insertToken(src)
                    else -> state.insertDefault(src, tokPos)
                }
            }

            TICK -> {
                val state2 = state.collapse(tokPos) { lyt -> lyt.isIndent }
                if (state2.stack?.layoutDelimiter == LayoutDelimiter.Tick) {
                    LayoutState(state2.stack.tail, state2.acc).insertToken(src)
                } else {
                    state.insertDefault(src, tokPos).pushStack(tokPos,
                        LayoutDelimiter.Tick
                    )
                }
            }

            STRING -> state
                .insertDefault(src, tokPos)
                .popStack { it: LayoutDelimiter -> it == LayoutDelimiter.Property }

            IF -> state.insertKwProperty(src, tokPos) { it.pushStack(tokPos,
                LayoutDelimiter.If
            ) }

            THEN -> {
                val state2 = state.collapse(tokPos) { lyt -> lyt.isIndent }
                if (state2.stack?.layoutDelimiter == LayoutDelimiter.If) {
                    LayoutState(state2.stack.tail, state2.acc)
                        .insertToken(src)
                        .pushStack(tokPos, LayoutDelimiter.Then)
                } else {
                    state
                        .insertDefault(src, tokPos)
                        .popStack { it: LayoutDelimiter -> it == LayoutDelimiter.Property }
                }
            }

            ELSE -> {
                val state2 = state.collapse(tokPos) { lyt -> lyt.isIndent }
                if (state2.stack?.layoutDelimiter == LayoutDelimiter.Then) {
                    state2.popStack().insertToken(src)
                } else {
                    val state3 = state.collapse(tokPos, ::offsideP)
                    if (state3.isTopDecl(tokPos)) {
                        state3.insertToken(src)
                    } else {
                        state3
                            .insertSep(tokPos)
                            .insertToken(src)
                            .popStack { it == LayoutDelimiter.Property }
                    }
                }
            }

            else -> state.insertDefault(src, tokPos)
        }
    }
}