package org.purescript.inference

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.purescript.getValueDeclarationGroupByName

class InferenceIntegrationTest: BasePlatformTestCase() {
    fun `test everything`() {
        val xScope = Scope(mutableMapOf(), mutableMapOf())
        val fScope = Scope(mutableMapOf(), mutableMapOf())
        val Main = myFixture.configureByText(
            "Main.purs",
            """
                | module Main where
                | 
                | f a = a
                | 
                | x = f 1
            """.trimMargin()
        )
        val f = Main.getValueDeclarationGroupByName("f")
        val x = Main.getValueDeclarationGroupByName("x")
        TestCase.assertEquals(
            Type.function(fScope.lookup("a"), fScope.lookup("a")),
            f.infer(fScope)
        )
        val xValue = x.valueDeclarations.single().value!!
        TestCase.assertEquals(Type.Int, xValue.infer(xScope))
    }
}