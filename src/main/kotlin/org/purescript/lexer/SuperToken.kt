package org.purescript.lexer

data class SuperToken(
    val qualified: List<Lexeme>,
    val token: Lexeme,
) {
    val tokens get() = qualified.flatMap { it.tokens } + token.tokens
    val start get() = qualified.firstOrNull()?.start ?: token.start
    val column get() = start.column
    val end get() = token.end
    val value = token.value
}