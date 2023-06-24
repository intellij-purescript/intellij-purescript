package org.purescript.inference

import junit.framework.TestCase

class InferenceTest: TestCase()  {
    fun `test inferApp`() {
        val scope = Scope.new()
        val a = scope.lookup("a")
        val id = InferType.function(a, a)
        assertEquals(InferType.Int, scope.inferApp(id, InferType.Int))
    }
    
    fun `test to string of function argument`() {
        val scope = Scope.new()
        val a = scope.lookup("a")
        val parameter = InferType.function(InferType.Int, a)
        val higherOrderFunction = InferType.function(parameter, a)
        assertEquals("(Int -> u0) -> u0", higherOrderFunction.toString())
    } 
    
    fun `test to string of Kind argument`() {
        val scope = Scope.new()
        val a = scope.lookup("a")
        val parameter = InferType.Array.app(a)
        val unsafeHead = InferType.function(parameter, a)
        assertEquals("Array u0 -> u0", unsafeHead.toString())
    }
}