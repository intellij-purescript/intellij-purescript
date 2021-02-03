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
}