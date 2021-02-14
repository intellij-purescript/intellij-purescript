package org.purescript.psi

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.purescript.file.PSFile

class PSValueDeclarationTest : BasePlatformTestCase() {
    fun `test finds doc comment`() {
        val file = myFixture.addFileToProject(
            "Main.purs",
            """-- | This is
               -- | main module
               module Main (x, y) where
               -- | This is
               -- | main
               main = 1
            """.trimIndent()
        ) as PSFile

        val main = file.module.valueDeclarationsByName["main"]!!.first()
        val docComments = main.docComments
        TestCase.assertEquals(2, docComments.size)
        TestCase.assertEquals("-- | This is",  docComments[0].text)
        TestCase.assertEquals("-- | main",  docComments[1].text)
    }
}