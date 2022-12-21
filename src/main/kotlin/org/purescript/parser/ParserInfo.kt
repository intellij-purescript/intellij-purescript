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
            .flatMapTo(mutableSetOf()) { it.expectedName ?: setOf() }
        val expected = expectedStrings.toTypedArray()
        if (expected.isNotEmpty()) {
            val sb = StringBuilder()
            sb.append("Expecting ")
            for (i in 0 until expected.size - 2) {
                sb.append(expected[i]).append(", ")
            }
            if (expected.size >= 2) {
                sb.append(expected[expected.size - 2]).append(" or ")
            }
            sb.append(expected[expected.size - 1])
            return sb.toString()
        }
        return "Error"
    }

    fun merge(other: ParserInfo, success: Boolean) = when {
        position < other.position -> {
            if (success == other.success) {
                other
            } else {
                ParserInfo(
                    other.position,
                    other.expected,
                    other.errorMessage,
                    success
                )
            }
        }
        position > other.position -> {
            if (success == this.success) {
                this
            } else {
                ParserInfo(
                    position,
                    expected,
                    errorMessage,
                    success
                )
            }
        }
        else -> ParserInfo(
            position,
            expected + other.expected,
            if (errorMessage == null) other.errorMessage
            else errorMessage + ";" + other.errorMessage,
            success
        )
    }
}