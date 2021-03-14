package org.purescript.lexer.token

data class SourcePos(
    val line: Int,
    val column: Int,
    val offset: Int,
) {
    fun bump(text: CharSequence, nextOffset: Int): SourcePos {
        val subSequence = text.subSequence(offset, nextOffset)
        return when {
            "\n" in subSequence -> {
                SourcePos(
                    line = line + subSequence.count { it == '\n'},
                    column = nextOffset - text.lastIndexOf("\n", nextOffset) - 1,
                    offset = nextOffset
                )
            }
            else -> {
                SourcePos(
                    line = line,
                    column = column + nextOffset,
                    offset = nextOffset
                )
            }
        }
    }
}