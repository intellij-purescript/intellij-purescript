package org.purescript.lexer

data class SuperToken(
    val qualified: List<Lexeme>,
    val token: Lexeme,
) {
    val tokens get() = qualified.flatMap { it.tokens } + token.tokens
    val start get() = qualified.firstOrNull()?.start ?: token.start
    val line get() = start.line
    val column get() = start.column
    val asSep get() = start.asSep
    val asEnd get() = start.asEnd
    val end get() = token.end
    val value = token.value
}