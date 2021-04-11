package org.purescript.psi.`var`

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.SyntaxTraverser
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.purescript.file.PSFile
import org.purescript.psi.PSForeignValueDeclaration

//class LocalForeignValueReferenceTest : BasePlatformTestCase() {
//    fun `test finds local variants`() {
//        val file = myFixture.configureByText(
//            "Main.purs",
//            """ module Main where
//
//                -- | Returns the substrings of the second string separated
//                foreign import split :: Pattern -> String -> Array String'
//
//                f = s
//            """.trimIndent()
//        ) as PSFile
//        val reference = file
//            .getVarByName("s")!!
//            .references
//            .find { it is LocalForeignValueReference }
//        val first : PsiNamedElement = reference!!.variants.first() as PSForeignValueDeclaration
//        TestCase.assertEquals("split", first.name)
//    }
//
//    fun `test finds local reference`() {
//        val file = myFixture.configureByText(
//            "Main.purs",
//            """ module Main where
//
//                -- | Returns the substrings of the second string separated
//                foreign import split :: Pattern -> String -> Array String'
//
//                f = split
//            """.trimIndent()
//        ) as PSFile
//        val reference = file
//            .getVarByName("split")!!
//            .references
//            .find { it is LocalForeignValueReference }!!
//        val first : PsiNamedElement = reference.resolve() as PSForeignValueDeclaration
//        TestCase.assertEquals("split", first.name)
//    }
//
//    private fun PsiElement.getVarByName(
//        name: String
//    ): PSVar? {
//        return SyntaxTraverser
//            .psiTraverser(this)
//            .filterIsInstance(PSVar::class.java)
//            .firstOrNull { it.text.trim() == name }
//    }
//}
