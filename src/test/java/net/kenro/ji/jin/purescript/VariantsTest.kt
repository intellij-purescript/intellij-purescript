package net.kenro.ji.jin.purescript

import com.intellij.psi.PsiNamedElement
import net.kenro.ji.jin.purescript.file.PSFile
import net.kenro.ji.jin.purescript.parser.PSLanguageParserTestBase
import net.kenro.ji.jin.purescript.psi.PSIdentifierImpl

class VariantsTest : PSLanguageParserTestBase() {
    fun testIdentifierCanResolveToParameter() {
        val file = createFile(
            "Main.purs",
            """
                  module Main where
                  z = 1
                  y x = x
                  """.trimIndent()
        ) as PSFile
        val psIdentifier: PSIdentifierImpl = file.findElementAt(30)!!.parent as PSIdentifierImpl
        val reference = psIdentifier.reference!!
        val resolved = reference
            .variants
            .asSequence()
            .map { (it as PsiNamedElement).name }
            .toList()
        assertContainsElements(resolved, "z", "x", "y")

    }

}