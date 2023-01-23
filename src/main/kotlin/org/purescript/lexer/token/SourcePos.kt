package org.purescript.lexer.token

import org.purescript.parser.LAYOUT_END
import org.purescript.parser.LAYOUT_SEP
import org.purescript.parser.LAYOUT_START

data class SourcePos(
    val line: Int,
    val column: Int,
    val offset: Int,
) {
    val asEnd get() = SourceToken(LAYOUT_END, this, this).asLexeme.asSuper
    val asStart get() = SourceToken(LAYOUT_START, this, this).asLexeme.asSuper
    val asSep get() = SourceToken(LAYOUT_SEP, this, this).asLexeme.asSuper
}