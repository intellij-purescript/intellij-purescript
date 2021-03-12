package org.purescript.psi.data

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.purescript.getDataDeclaration

class PSDataDeclarationTest : BasePlatformTestCase() {

    fun `test empty data declaration`() {
        val dataDeclaration = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                data Bar
            """.trimIndent()
        ).getDataDeclaration()

        assertEquals("Bar", dataDeclaration.name)
        assertNull(dataDeclaration.dataConstructorList)
    }

    fun `test simple data declaration`() {
        val dataDeclaration = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                data Bar = Qux Int
            """.trimIndent()
        ).getDataDeclaration()

        assertEquals("Bar", dataDeclaration.name)
        assertNotNull(dataDeclaration.dataConstructorList)
    }
}
