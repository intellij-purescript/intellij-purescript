package org.purescript.psi

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.purescript.file.PSFile

class PSModuleTest : BasePlatformTestCase() {
    fun `test one word name`() {
        val file = myFixture.addFileToProject(
            "Main.purs",
            """module Main where"""
        ) as PSFile
        TestCase.assertEquals("Main", file.module.name)
    }

    fun `test two word name`() {
        val file = myFixture.addFileToProject(
            "Main.purs",
            """module My.Main where"""
        ) as PSFile
        TestCase.assertEquals("My.Main", file.module.name)
    }
    fun `test be able to find no exported names`() {
        val file = myFixture.addFileToProject(
            "Main.purs",
            """module My.Main where"""
        ) as PSFile
        TestCase.assertEquals(0, file.module.exportedNames.size)
    }
    fun `test be able to find one exported names`() {
        val file = myFixture.addFileToProject(
            "Main.purs",
            """
            module My.Main (x) where
            x  =1
            """.trimIndent()
        ) as PSFile
        TestCase.assertEquals(1, file.module.exportedNames.size)
    }

    fun `test be able to find two exported names`() {
        val file = myFixture.addFileToProject(
            "Main.purs",
            """module My.Main (x, y) where
               x = 1
               y = 2
            """.trimIndent()
        ) as PSFile
        TestCase.assertEquals(2, file.module.exportedNames.size)
        assertContainsElements(file.module.exportedNames, "x", "y")
    }

    fun `test finds doc comment`() {
        val file = myFixture.addFileToProject(
            "Main.purs",
            """-- | This is
               -- | main
               module My.Main (x, y) where
            """.trimIndent()
        ) as PSFile

        TestCase.assertEquals(2, file.module.docComments.size)
        TestCase.assertEquals("-- | This is", file.module.docComments[0].text)
    }
}
