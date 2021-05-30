package org.purescript.lexer.token

data class SourceRange(
    val start: SourcePos,
    val end: SourcePos
)