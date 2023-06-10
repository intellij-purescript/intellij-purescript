package org.purescript.typechecker

import junit.framework.TestCase

class TypeCheckerTest : TestCase() {
    fun `test function helper method`() {
        assertEquals(
            "Prim.Int",
            TypeCheckerType.function(Prim.int).toString()
        )
        assertEquals(
            "Prim.Int -> Prim.String",
            TypeCheckerType.function(Prim.int, Prim.string).toString()
        )
        assertEquals(
            "Prim.Int -> Prim.Int -> Prim.String",
            TypeCheckerType.function(Prim.int, Prim.int, Prim.string).toString()
        )
    }
}