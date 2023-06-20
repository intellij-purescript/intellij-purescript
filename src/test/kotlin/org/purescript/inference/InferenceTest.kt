package org.purescript.inference

import junit.framework.TestCase

class InferenceTest: TestCase()  {
    fun `test everything`() {
        val scope = Scope(mutableMapOf(), mutableMapOf())
        val a = scope.lookup("a")
        val id = Type.function(a, a)
        assertEquals(Type.Int, scope.inferApp(id, Type.Int))
    }
}