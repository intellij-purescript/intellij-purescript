package org.purescript.lexer

import org.purescript.lexer.LayoutDelimiter.CaseBinders
import org.purescript.lexer.token.SourcePos
import org.purescript.parser.LAYOUT_END
import org.purescript.parser.LAYOUT_SEP

data class LayoutState(
    val stack: LayoutStack?,
    val acc: List<Pair<SuperToken, LayoutStack?>>
) {
    inline fun collapse(tokPos: SourcePos, p: (LayoutDelimiter) -> Boolean)
        : LayoutState = this.collapse(tokPos) { _, _, lyt -> p(lyt) }

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
                val pair = lytToken(tokPos, LAYOUT_END) to stack.tail
                acc = acc.toMutableList() + pair
            }
            stack = stack.tail
        }
        return LayoutState(stack, acc)
    }

    fun insertToken(token: SuperToken) =
        copy(acc = acc.toMutableList() + (token to stack))

    fun pushStack(lytPos: SourcePos, lyt: LayoutDelimiter) =
        copy(stack = LayoutStack(lytPos, lyt, stack))

    fun insertDefault(src: SuperToken, tokPos: SourcePos) =
        collapse(tokPos, ::offsideP).insertSep(tokPos).insertToken(src)

    inline fun popStack(p: (LayoutDelimiter) -> Boolean): LayoutState = when {
        stack == null -> this
        p(stack.layoutDelimiter) -> copy(stack = stack.tail)
        else -> this
    }

    fun insertSep(tokPos: SourcePos): LayoutState {
        val (lytPos, lyt, tail) = this.stack ?: return this
        val sepTok = lytToken(tokPos, LAYOUT_SEP)
        return when {
            LayoutDelimiter.TopDecl == lyt && sepP(tokPos, lytPos) ->
                copy(stack = tail).insertToken(sepTok)

            LayoutDelimiter.TopDeclHead == lyt && sepP(tokPos, lytPos) ->
                copy(stack = tail).insertToken(sepTok)

            identSepP(tokPos, lytPos, lyt) -> when (lyt) {
                LayoutDelimiter.Of ->
                    insertToken(sepTok).pushStack(tokPos, CaseBinders)

                else -> insertToken(sepTok)
            }

            else -> this
        }
    }
}