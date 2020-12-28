package org.purescript.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
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
        val psVar = getVarByName(file, "y")!!
        val references =  psVar.references
        val valueDeclaration = references
            .map {it.resolve()}
            .filterIsInstance(PSValueDeclaration::class.java)
            .first()
        TestCase.assertEquals("y", valueDeclaration.name)
    }

    fun `test var can see all value declarations`() {
        val file = createFile(
            "Main.purs",
            """
            module Main where
            x = y
            y = 1
            """.trimIndent()
        ) as PSFile
        val psVar = getVarByName(file, "y")!!
        val references =  psVar.references
        val valueReference = references
            .filterIsInstance(ValueReference::class.java)
            .first()
        val names = valueReference.variants.map { (it as PsiNamedElement).name }
        assertContainsElements(names, "x", "y")
    }

    private fun getVarByName(
        file: PSFile,
        name: String
    ): PSVar? {
        return file.findDescendantOfType({ true }, { it.text.trim() == name})
    }
}