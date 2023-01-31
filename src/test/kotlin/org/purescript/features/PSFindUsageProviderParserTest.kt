package org.purescript.features

import junit.framework.TestCase
import org.purescript.file.PSFile
import org.purescript.getValueDeclarationGroupByName
import org.purescript.parser.PSLanguageParserTestBase

class PSFindUsageProviderParserTest : PSLanguageParserTestBase("parser") {

    fun `test descriptive name include module name`() {
        val provider = PSFindUsageProvider()
        val x = createFile(
            "Main.purs",
            """module Main where
                    |x = 1
                """.trimMargin()
        ).getValueDeclarationGroupByName("x")
        TestCase.assertEquals("Main.x", provider.getDescriptiveName(x))
    }

    fun `test full name include module name`() {
        val provider = PSFindUsageProvider()
        val x = createFile(
            "Main.purs",
            """module Main where
                    |x = 1
                """.trimMargin()
        ).getValueDeclarationGroupByName("x")
        TestCase.assertEquals("Main.x", provider.getNodeText(x, true))
    }
}