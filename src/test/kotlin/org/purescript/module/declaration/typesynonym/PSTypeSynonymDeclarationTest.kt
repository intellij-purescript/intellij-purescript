package org.purescript.module.declaration.typesynonym

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.purescript.getTypeSynonymDeclaration

class PSTypeSynonymDeclarationTest : BasePlatformTestCase() {

    fun `test get name`() {
        val typeSynonymDeclaration = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                type Bar = Int
            """.trimIndent()
        ).getTypeSynonymDeclaration()

        assertEquals("Bar", typeSynonymDeclaration.name)
    }
}
