package org.purescript.parser

abstract class ParserInfo(
    val expected: Set<Parsec>
) {

    class Failure(val position: Int, expected: Set<Parsec>) : ParserInfo(expected)
    class Optional(val position: Int, expected: Set<Parsec>) : ParserInfo(expected)
    class Success : ParserInfo(emptySet())

    override fun toString(): String {
        val expected = expected.flatMap { it.expectedName }.toSet().toList()
        return "Expected one of: ${expected.joinToString(", ") { it }}"
    }
}