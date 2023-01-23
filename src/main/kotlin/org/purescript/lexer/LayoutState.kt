package org.purescript.lexer

import org.purescript.lexer.LayoutDelimiter.*
import org.purescript.lexer.token.SourcePos

data class LayoutState(
    val stack: LayoutStack?,
    val acc: List<Pair<SuperToken, LayoutStack?>>
) {
    fun isTopDecl(tokPos: SourcePos) = stack?.isTopDecl(tokPos) == true
    inline fun collapse(tokPos: SourcePos, p: (LayoutDelimiter) -> Boolean) =
        collapse(tokPos) { _, _, lyt -> p(lyt) }

    fun insertStart(nextPos: SourcePos, lyt: LayoutDelimiter): LayoutState =
        when (val indent = stack?.find { it.isIndent }) {
            null -> pushStack(nextPos, lyt)
                .insertToken(nextPos.asStart)

            else -> when {
                nextPos.column <= indent.sourcePos.column -> this
                else -> pushStack(nextPos, lyt).insertToken(nextPos.asStart)
            }
        }

    inline fun insertKwProperty(
        src: SuperToken,
        tokPos: SourcePos,
        k: (LayoutState) -> LayoutState
    ) = when (stack?.layoutDelimiter) {
        Property -> insertDefault(src, tokPos).popStack()
        else -> k(insertDefault(src, tokPos))
    }

    inline fun collapse(
        tokPos: SourcePos,
        p: (SourcePos, SourcePos, LayoutDelimiter) -> Boolean
    ): LayoutState {
        var (stack, acc) = this
        while (
            stack != null &&
            p(tokPos, stack.sourcePos, stack.layoutDelimiter)
        ) {
            if (stack.layoutDelimiter.isIndent) {
                acc = acc + (tokPos.asEnd to stack.tail)
            }
            stack = stack.tail
        }
        return LayoutState(stack, acc)
    }

    fun insertToken(token: SuperToken) = copy(acc = acc + (token to stack))
    fun pushStack(lytPos: SourcePos, lyt: LayoutDelimiter) =
        copy(stack = LayoutStack(lytPos, lyt, stack))

    fun insertDefault(src: SuperToken, tokPos: SourcePos) =
        collapse(tokPos, ::offsideP).insertSep(tokPos).insertToken(src)

    fun popStack() = popStack { true }
    inline fun popStack(p: (LayoutDelimiter) -> Boolean): LayoutState = when {
        stack == null -> this
        p(stack.layoutDelimiter) -> copy(stack = stack.tail)
        else -> this
    }

    fun insertSep(tokPos: SourcePos): LayoutState {
        val (srcPos, lyt, _) = stack ?: return this
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
}