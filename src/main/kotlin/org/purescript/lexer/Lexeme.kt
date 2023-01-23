package org.purescript.lexer

import org.purescript.lexer.token.SourceToken

data class Lexeme(
    val token: SourceToken,
    val trailingWhitespace: List<SourceToken>
) {
    val tokens get() = listOf(token) + trailingWhitespace
    val start get() = token.start
    val end get() = trailingWhitespace.lastOrNull()?.end ?: token.end
    val value = token.value
    val asSuper get() = SuperToken(emptyList(), this)
}