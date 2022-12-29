package org.purescript.features

import junit.framework.TestCase
import org.purescript.file.PSFile
import org.purescript.parser.PSLanguageParserTestBase

class PSFindUsageProviderParserTest : PSLanguageParserTestBase("parser") {

    fun `test descriptive name include module name`() {
        val provider = PSFindUsageProvider()
        val file = createFile(
            "Main.purs",
            """module Main where
                |x = 1
            """.trimMargin()
        ) as PSFile.Psi
        val x = file.topLevelValueDeclarations["x"]!!.first()
        TestCase.assertEquals("Main.x", provider.getDescriptiveName(x))
    }

    fun `test full name include module name`() {
        val provider = PSFindUsageProvider()
        val file = createFile(
            "Main.purs",
            """module Main where
                |x = 1
            """.trimMargin()
        ) as PSFile.Psi
        val x = file.topLevelValueDeclarations["x"]!!.first()
        TestCase.assertEquals("Main.x", provider.getNodeText(x, true))
    }
}