package org.purescript.psi.expression

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.purescript.getExpressionConstructor

class PSExpressionConstructorTest : BasePlatformTestCase() {
    fun `test gets name`() {
        val expressionConstructor = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                val = Bar
            """.trimIndent()
        ).getExpressionConstructor()

        assertEquals("Bar", expressionConstructor.name)
    }

    fun `test gets qualified name`() {
        val expressionConstructor = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                val = Qux.Bar
            """.trimIndent()
        ).getExpressionConstructor()

        assertEquals("Bar", expressionConstructor.name)
    }
}
