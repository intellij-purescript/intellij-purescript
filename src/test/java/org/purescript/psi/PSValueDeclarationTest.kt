package org.purescript.psi

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.purescript.getModule
import org.purescript.getValueDeclaration

class PSValueDeclarationTest : BasePlatformTestCase() {
    fun `test finds doc comment`() {
        val valueDeclaration = myFixture.addFileToProject(
            "Main.purs",
            """-- | This is
               -- | main module
               module Main (x, y) where
               -- | This is
               -- | main
               main = 1
            """.trimIndent()
        ).getValueDeclaration()
        val docComments = valueDeclaration.docComments

        assertEquals(2, docComments.size)
        assertEquals("-- | This is", docComments[0].text)
        assertEquals("-- | main", docComments[1].text)
    }
}
