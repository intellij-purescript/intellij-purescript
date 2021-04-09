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

    companion object {

        fun merge(
            info1: ParserInfo,
            info2: ParserInfo,
            success: Boolean
        ): ParserInfo {
            return if (info1.position < info2.position) {
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
            } else if (info1.position > info2.position) {
                if (success == info1.success) {
                    info1
                } else {
                    ParserInfo(
                        info1.position,
                        info1.expected,
                        info1.errorMessage,
                        success
                    )
                }
            } else {
                ParserInfo(
                    info1.position,
                    info1.expected + info2.expected,
                    if (info1.errorMessage == null) info2.errorMessage
                    else info1.errorMessage + ";" + info2.errorMessage,
                    success
                )
            }
        }
    }
}