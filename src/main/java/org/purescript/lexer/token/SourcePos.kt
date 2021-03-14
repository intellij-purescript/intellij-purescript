package org.purescript.lexer.token

data class SourcePos(
    val line: Int,
    val column: Int,
    val offset: Int,
)