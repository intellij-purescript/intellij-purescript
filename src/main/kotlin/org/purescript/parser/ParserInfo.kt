package org.purescript.parser

data class ParserInfo(
    val position: Int,
    val expected: Set<Parsec>,
    val success: Boolean
) {

    override fun toString(): String {
        val expected = expected.flatMap { it.expectedName }.toSet().toList()
        return "Expected one of: ${expected.joinToString(", ") { it }}"
    }

    fun merge(other: ParserInfo) = when {
        position < other.position -> other
        other.position < position -> {
            if (other.success == this.success) {
                this
            } else {
                copy(success = other.success)
            }
        }
        else -> ParserInfo(position, expected + other.expected, other.success)
    }
}