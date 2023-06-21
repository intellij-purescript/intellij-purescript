package org.purescript.inference

import junit.framework.TestCase

class InferenceTest: TestCase()  {
    fun `test inferApp`() {
        val scope = Scope.new()
        val a = scope.lookup("a")
        val id = Type.function(a, a)
        assertEquals(Type.Int, scope.inferApp(id, Type.Int))
    }
    
    fun `test to string of function argument`() {
        val scope = Scope.new()
        val a = scope.lookup("a")
        val parameter = Type.function(Type.Int, a)
        val higherOrderFunction = Type.function(parameter, a)
        assertEquals("(Int -> u0) -> u0", higherOrderFunction.toString())
    } 
    
    fun `test to string of Kind argument`() {
        val scope = Scope.new()
        val a = scope.lookup("a")
        val parameter = Type.Array.app(a)
        val unsafeHead = Type.function(parameter, a)
        assertEquals("Array u0 -> u0", unsafeHead.toString())
    }
}