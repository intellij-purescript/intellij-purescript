package org.purescript.lexer.token

import com.intellij.psi.tree.IElementType
import org.purescript.lexer.Lexeme

data class SourceToken(
    val value: IElementType,
    val start: SourcePos,
    val end: SourcePos,
) {
    val asLexeme get() = Lexeme(this, emptyList())
}