package org.purescript.lexer

import org.purescript.lexer.LayoutDelimiter.*
import org.purescript.lexer.token.SourcePos

data class LayoutState(
    val stack: LayoutStack,
    val acc: List<Pair<SuperToken, LayoutStack>>
) {
    fun isTopDecl(tokPos: SourcePos) = stack.isTopDecl(tokPos)
    inline fun collapse(tokPos: SourcePos, p: (LayoutDelimiter) -> Boolean) =
        collapse(tokPos) { _, _, lyt -> p(lyt) }

    fun insertStart(nextPos: SourcePos, lyt: LayoutDelimiter): LayoutState =
        when (val indent = stack.find { it.isIndent }) {
            null -> pushStack(nextPos, lyt).insertToken(nextPos.asStart)
            else -> when {
                nextPos.column <= indent.sourcePos.column -> this
                else -> pushStack(nextPos, lyt).insertToken(nextPos.asStart)
            }
        }

    inline fun insertKwProperty(
        t: SuperToken,
        k: (LayoutState) -> LayoutState
    ) = when (stack.layoutDelimiter) {
        Property -> insertDefault(t).popStack()
        else -> k(insertDefault(t))
    }

    inline fun collapse(
        tokPos: SourcePos,
        p: (SourcePos, SourcePos, LayoutDelimiter) -> Boolean
    ): LayoutState {
        var (stack, acc) = this
        while (
            stack.tail != null &&
            p(tokPos, stack.sourcePos, stack.layoutDelimiter)
        ) {
            if (stack.layoutDelimiter.isIndent) {
                acc = acc + (tokPos.asEnd to (stack.tail as LayoutStack))
            }
            stack = stack.tail as LayoutStack
        }
        return LayoutState(stack, acc)
    }

    fun insertToken(token: SuperToken) = copy(acc = acc + (token to stack))
    fun pushStack(lytPos: SourcePos, lyt: LayoutDelimiter) =
        copy(stack = stack.push(lytPos, lyt))

    fun insertDefault(src: SuperToken) =
        collapse(src.start, ::offsideP).insertSep(src.start).insertToken(src)

    fun popStack() = popStack { true }
    inline fun popStack(p: (LayoutDelimiter) -> Boolean): LayoutState = when {
        stack.tail == null -> this
        p(stack.layoutDelimiter) -> copy(stack = stack.tail)
        else -> this
    }

    fun insertSep(tokPos: SourcePos): LayoutState {
        val (srcPos, lyt, _) = stack
        return when {
            tokPos.column != srcPos.column || tokPos.line == srcPos.line -> this
            TopDecl == lyt || TopDeclHead == lyt ->
                popStack().insertToken(tokPos.asSep)

            Of == lyt ->
                insertToken(tokPos.asSep).pushStack(tokPos, CaseBinders)

            lyt.isIndent -> insertToken(tokPos.asSep)
            else -> this
        }
    }

    fun toPair(): Pair<LayoutStack, List<SuperToken>> {
        return stack to acc.map {it.first}
    }
}