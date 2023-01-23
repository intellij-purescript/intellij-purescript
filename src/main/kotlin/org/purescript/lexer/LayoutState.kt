package org.purescript.lexer

import org.purescript.lexer.LayoutDelimiter.*
import org.purescript.lexer.token.SourcePos
import org.purescript.parser.LAYOUT_END
import org.purescript.parser.LAYOUT_SEP
import org.purescript.parser.LAYOUT_START

data class LayoutState(
    val stack: LayoutStack?,
    val acc: List<Pair<SuperToken, LayoutStack?>>
) {
    inline fun collapse(tokPos: SourcePos, p: (LayoutDelimiter) -> Boolean) =
        collapse(tokPos) { _, _, lyt -> p(lyt) }

    fun insertStart(nextPos: SourcePos, lyt: LayoutDelimiter): LayoutState =
        when (val indent = stack?.find { it.isIndent }) {
            null -> pushStack(nextPos, lyt)
                .insertToken(lytToken(nextPos, LAYOUT_START))

            else -> when {
                nextPos.column <= indent.sourcePos.column -> this
                else -> pushStack(nextPos, lyt)
                    .insertToken(lytToken(nextPos, LAYOUT_START))
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
                acc = acc + (lytToken(tokPos, LAYOUT_END) to stack.tail)
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
        val (_, lyt, _) = stack ?: return this
        val sepTok = lytToken(tokPos, LAYOUT_SEP)
        return when {
            TopDecl == lyt && sepP(tokPos, stack.sourcePos) ->
                popStack().insertToken(sepTok)

            TopDeclHead == lyt && sepP(tokPos, stack.sourcePos) ->
                popStack().insertToken(sepTok)

            lyt.isIndent && sepP(tokPos, stack.sourcePos) -> when (lyt) {
                Of -> insertToken(sepTok).pushStack(tokPos, CaseBinders)
                else -> insertToken(sepTok)
            }

            else -> this
        }
    }
}