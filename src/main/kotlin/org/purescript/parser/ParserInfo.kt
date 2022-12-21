package org.purescript.parser

class ParserInfo(
    val position: Int,
    val expected: Set<Parsec>,
    private val errorMessage: String?,
    val success: Boolean
) {

    override fun toString(): String {
        if (errorMessage != null) return errorMessage
        val expectedStrings: Set<String> = expected
            .flatMapTo(mutableSetOf()) { it.expectedName }
        val expected = expectedStrings.toTypedArray()
        return if (expected.isNotEmpty()) buildString {
            append("Expecting ")
            for (i in 0 until expected.size - 2) {
                append("${expected[i]}, ")
            }
            if (expected.size >= 2) {
                append("${expected[expected.size - 2]} or ")
            }
            append(expected[expected.size - 1])
        } else {
            "Error"
        }
    }

    fun merge(other: ParserInfo) = when {
        position < other.position -> other
        other.position < position -> {
            if (other.success == this.success) {
                this
            } else {
                ParserInfo(
                    position,
                    expected,
                    errorMessage,
                    other.success
                )
            }
        }
        else -> ParserInfo(
            position,
            expected + other.expected,
            if (errorMessage == null) other.errorMessage
            else errorMessage + ";" + other.errorMessage,
            other.success
        )
    }
}