package org.purescript.psi

import com.intellij.psi.util.findDescendantOfType
import junit.framework.TestCase
import org.purescript.file.PSFile
import org.purescript.parser.PSLanguageParserTestBase

class PSVarTest : PSLanguageParserTestBase() {

    fun `test var can resolve to top level`() {
        val file = createFile(
            "Main.purs",
            """
            module Main where
            x = y
            y = 1
            """.trimIndent()
        ) as PSFile
        val psVar =
            file.findDescendantOfType<PSVar>(
                { psVar -> true }
            )
        val reference =  psVar!!.reference
        val resolve = reference.resolve() as PSValueDeclaration
        TestCase.assertEquals("y", resolve.name)
    }
}