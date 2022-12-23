package org.purescript.parser

abstract class ParserInfo(
    val position: Int,
    val expected: Set<Parsec>
) {

    class Optional(position: Int, expected: Set<Parsec>) : ParserInfo(position, expected)
    class Success(position: Int, expected: Set<Parsec>) : ParserInfo(position, expected)
    class Failure(position: Int, expected: Set<Parsec>) : ParserInfo(position, expected) 

    override fun toString(): String {
        val expected = expected.flatMap { it.expectedName }.toSet().toList()
        return "Expected one of: ${expected.joinToString(", ") { it }}"
    }
}