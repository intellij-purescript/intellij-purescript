package org.purescript.psi.data

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.purescript.getDataConstructor

class PSDataConstructorTest : BasePlatformTestCase() {

    fun `test parses simple constructor`() {
        val dataConstructor = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                data Bar = Qux
            """.trimIndent()
        ).getDataConstructor()

        assertEquals("Qux", dataConstructor.name)
    }

    fun `test parses constructor with type var`() {
        val dataConstructor = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                data Bar a = Qux a
            """.trimIndent()
        ).getDataConstructor()

        assertEquals("Qux", dataConstructor.name)
        assertSize(1, dataConstructor.typeAtoms)
    }

    fun `test parses constructor with parenthesized type atoms`() {
        val dataConstructor = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                data Bar = Qux (List Int)
            """.trimIndent()
        ).getDataConstructor()

        assertEquals("Qux", dataConstructor.name)
        assertSize(1, dataConstructor.typeAtoms)
    }
}
