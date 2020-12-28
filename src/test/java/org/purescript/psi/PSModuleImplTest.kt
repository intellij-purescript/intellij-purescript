package org.purescript.psi

import junit.framework.TestCase
import org.purescript.file.PSFile
import org.purescript.parser.PSLanguageParserTestBase

class PSModuleImplTest : PSLanguageParserTestBase() {
    fun `test one word name`() {
        val file = createFile(
            "Main.purs",
            """module Main where"""
        ) as PSFile
        TestCase.assertEquals("Main", file.module.name)
    }

    fun `test two word name`() {
        val file = createFile(
            "Main.purs",
            """module My.Main where"""
        ) as PSFile
        TestCase.assertEquals("My.Main", file.module.name)
    }
}