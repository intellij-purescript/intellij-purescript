package org.purescript.parser

class ParserInfo {
    val position: Int
    val expected: Set<Parsec>
    val errorMessage: String?
    val success: Boolean

    private constructor(
        position: Int,
        expected: Set<Parsec>,
        errorMessage: String?,
        success: Boolean
    ) {
        this.position = position
        this.success = success
        this.expected = expected
        this.errorMessage = errorMessage
    }

    constructor(position: Int, errorMessage: String?, b: Boolean) {
        this.position = position
        success = b
        expected = setOf()
        this.errorMessage = errorMessage
    }

    constructor(position: Int, expected: Parsec, success: Boolean) : this(
        position,
        setOf(expected),
        null,
        success
    )

    constructor(position: Int, info: ParserInfo, success: Boolean) : this(
        position,
        info.expected,
        null,
        success
    )

    override fun toString(): String {
        if (errorMessage != null) return errorMessage
        var expectedStrings: Set<String> = setOf()
        for (parsec in expected) {
            expectedStrings = expectedStrings + (parsec.expectedName!!)
        }
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

    fun merge(info2: ParserInfo, success: Boolean) =
        if (position < info2.position) {
            if (success == info2.success) {
                info2
            } else {
                ParserInfo(
                    info2.position,
                    info2.expected,
                    info2.errorMessage,
                    success
                )
            }
        } else if (position > info2.position) {
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
        } else {
            ParserInfo(
                position,
                expected + info2.expected,
                if (errorMessage == null) info2.errorMessage
                else errorMessage + ";" + info2.errorMessage,
                success
            )
        }
}