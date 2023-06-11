package org.purescript.typechecker

import junit.framework.TestCase

private val typeVarA = TypeCheckerType.TypeVar("a")

class TypeCheckerTest : TestCase() {
    private val intToString = TypeCheckerType.function(Prim.int, Prim.string)

    fun `test function helper method`() {
        assertEquals(
            "Prim.Int",
            TypeCheckerType.function(Prim.int).toString()
        )
        assertEquals(
            "Prim.Int -> Prim.String",
            intToString.toString()
        )
        assertEquals(
            "Prim.Int -> Prim.Int -> Prim.String",
            TypeCheckerType.function(Prim.int, Prim.int, Prim.string).toString()
        )
    }

    fun `test call of function without for all`() {
        assertEquals(Prim.string, intToString.call(Prim.int))
        assertEquals(null, intToString.call(Prim.string))
    }

    fun `test call of function with for all`() {
        val function = TypeCheckerType.ForAll("a", intToString)
        assertEquals(Prim.string, function.call(Prim.int))
    }

    fun `test substitution`() {
        assertEquals(Prim.int, typeVarA.substitute("a", Prim.int))
        assertEquals(typeVarA, typeVarA.substitute("b", Prim.int))
        assertEquals(
            TypeCheckerType.function(Prim.int, Prim.int),
            TypeCheckerType.function(typeVarA, typeVarA).substitute("a", Prim.int)
        )
    }

    fun `test call of identity function`() {
        val identityFunction = TypeCheckerType.ForAll("a", TypeCheckerType.function(typeVarA, typeVarA))
        assertEquals(Prim.int, identityFunction.call(Prim.int))
    }
}