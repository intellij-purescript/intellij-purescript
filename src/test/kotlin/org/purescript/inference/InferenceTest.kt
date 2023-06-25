package org.purescript.inference

import junit.framework.TestCase

class InferenceTest: TestCase()  {
    fun `test to string of function argument`() {
        val a = IdGenerator().newId()
        val parameter = InferType.function(InferType.Int, a)
        val higherOrderFunction = InferType.function(parameter, a)
        assertEquals("(Int -> u0) -> u0", higherOrderFunction.toString())
    } 
    
    fun `test to string of Kind argument`() {
        val a = IdGenerator().newId()
        val parameter = InferType.Array.app(a)
        val unsafeHead = InferType.function(parameter, a)
        assertEquals("Array u0 -> u0", unsafeHead.toString())
    }
}