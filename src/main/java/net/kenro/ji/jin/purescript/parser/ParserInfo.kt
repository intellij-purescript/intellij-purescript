package net.kenro.ji.jin.purescript.parser

import java.util.*

class ParserInfo {
    val position: Int
    val expected: LinkedHashSet<Parsec>
    val errorMessage: String?
    val success: Boolean

    private constructor(
        position: Int,
        expected: LinkedHashSet<Parsec>,
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
        expected = LinkedHashSet()
        this.errorMessage = errorMessage
    }

    constructor(position: Int, expected: Parsec, success: Boolean) : this(
        position,
        set<Parsec>(expected),
        null,
        success
    ) {
    }

    constructor(position: Int, info: ParserInfo, success: Boolean) : this(
        position,
        info.expected,
        null,
        success
    ) {
    }

    override fun toString(): String {
        if (errorMessage != null) return errorMessage
        val expectedStrings = LinkedHashSet<String?>()
        for (parsec in expected) {
            expectedStrings.addAll(parsec.expectedName!!)
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
        private fun <T> set(obj: T): LinkedHashSet<T> {
            val result = LinkedHashSet<T>()
            result.add(obj)
            return result
        }

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
                val position = info1.position
                val expected = LinkedHashSet<Parsec>()
                expected.addAll(info1.expected)
                expected.addAll(info2.expected)
                ParserInfo(
                    position,
                    expected,
                    if (info1.errorMessage == null) info2.errorMessage else info1.errorMessage + ";" + info2.errorMessage,
                    success
                )
            }
        }
    }
}