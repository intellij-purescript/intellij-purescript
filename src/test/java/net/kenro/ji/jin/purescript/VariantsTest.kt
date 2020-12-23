package net.kenro.ji.jin.purescript

import com.intellij.psi.PsiNamedElement
import com.intellij.psi.util.PsiTreeUtil
import net.kenro.ji.jin.purescript.file.PSFile
import net.kenro.ji.jin.purescript.parser.PSLanguageParserTestBase
import net.kenro.ji.jin.purescript.psi.impl.PSDataDeclarationImpl
import net.kenro.ji.jin.purescript.psi.impl.PSIdentifierImpl
import net.kenro.ji.jin.purescript.psi.impl.PSValueDeclarationImpl
import org.junit.Ignore

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
        val psIdentifier:PSIdentifierImpl = file.findElementAt(30)!!.parent as PSIdentifierImpl
        val reference = psIdentifier.reference!!
        val resolved = reference
            .variants
            .asSequence()
            .map { (it as PsiNamedElement).name }
            .toList()
        assertContainsElements(resolved, "z", "x", "y")

    }

}