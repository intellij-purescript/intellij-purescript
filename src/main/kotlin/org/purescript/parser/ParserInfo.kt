package org.purescript.parser

abstract class ParserInfo(
    val position: Int,
    val expected: Set<Parsec>,
    val success: Boolean
) {
    class Optional(position: Int, expected: Set<Parsec>) : ParserInfo(position, expected, true)
    class Success(position: Int, expected: Set<Parsec>) : ParserInfo(position, expected, true)
    class Failure(position: Int, expected: Set<Parsec>) : ParserInfo(position, expected, false)
    override fun toString(): String {
        val expected = expected.flatMap { it.expectedName }.toSet().toList()
        return "Expected one of: ${expected.joinToString(", ") { it }}"
    }
}