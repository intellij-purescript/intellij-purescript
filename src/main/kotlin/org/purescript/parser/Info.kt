package org.purescript.parser

sealed interface Info {

    class Failure(val position: Int, val expected: Set<Parsec>) : Info
    class Optional(val position: Int, val expected: Set<Parsec>) : Info
    object Success : Info

}