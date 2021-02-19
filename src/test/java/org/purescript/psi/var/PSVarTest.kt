package org.purescript.psi.`var`

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.SyntaxTraverser
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.purescript.file.PSFile

class PSVarTest : BasePlatformTestCase() {

    fun `test var can resolve to top level`() {
        val file = myFixture.addFileToProject(
            "Main.purs",
            """
            module Main where
            x = y
            y = 1
            """.trimIndent()
        ) as PSFile
        val psVar = file.getVarByName("y")!!
        val valueReference = psVar.referenceOfType(LocalValueReference::class.java)
        val valueDeclaration = valueReference.resolve()!! as PsiNamedElement
        TestCase.assertEquals("y", valueDeclaration.name)
    }

    fun `test var can resolve to top level with multiple definitions`() {
        val file = myFixture.addFileToProject(
            "Main.purs",
            """
            module Main where
            x = y 1
            y 1 = 1
            y _ = 2
            """.trimIndent()
        ) as PSFile
        val psVar = file.getVarByName("y")!!
        val valueReference = psVar.referenceOfType(LocalValueReference::class.java)
        val valueDeclarations = valueReference.multiResolve(true)
        TestCase.assertEquals(2, valueDeclarations.size)
    }

    fun `test var can see variants for all value declarations`() {
        val file = myFixture.addFileToProject(
            "Main.purs",
            """
            module Main where
            
            import Foo
            import Bar hiding (w)
            import Baz (k)
            
            x = y
            y = 1
            """.trimIndent()
        ) as PSFile
        myFixture.addFileToProject(
            "Foo.purs",
            """
            module Foo (z) where
            z = 1
            """.trimIndent()
        )
        myFixture.addFileToProject(
            "Bar.purs",
            """
            module Bar (w, q) where
            w = 1
            q = 1
            """.trimIndent()
        )
        myFixture.addFileToProject(
            "Baz.purs",
            """
            module Baz (k, p) where
            k = 1
            p = 1
            """.trimIndent()
        )
        val psVar = file.getVarByName("y")!!
        val valueReference = psVar.referenceOfType(ImportedValueReference::class.java)
        val names = valueReference.variants.map { it.name }.toList()
        assertContainsElements(names, "z", "q", "k")
        assertDoesntContain(names, "w", "p")
    }

    fun `test var can resolve to imported files`() {
        myFixture.addFileToProject(
            "Lib.purs",
            """
            module Lib (y) where
            y = 1
            """.trimIndent()
        ) as PSFile
        val file = myFixture.addFileToProject(
            "Main.purs",
            """
            module Main where
            import Lib
            x = y
            """.trimIndent()
        ) as PSFile
        val psVar = file.getVarByName("y")!!
        val valueReference =
            psVar.referenceOfType(ImportedValueReference::class.java)
        val valueDeclaration = valueReference
            .multiResolve(false)
            .first().element as PsiNamedElement
        TestCase.assertEquals("y", valueDeclaration.name)
    }

    fun `test var can resolve to imported files which exports using module keyword`() {
        myFixture.addFileToProject(
            "Y.purs",
            """
            module Y (y) where
            y = 1
            """.trimIndent()
        ) as PSFile
        myFixture.addFileToProject(
            "Lib.purs",
            """
            module Lib (module Y) where
            import Y
            """.trimIndent()
        ) as PSFile
        val file = myFixture.addFileToProject(
            "Main.purs",
            """
            module Main where
            import Lib
            x = y
            """.trimIndent()
        ) as PSFile
        val psVar = file.getVarByName("y")!!
        val valueReference =
            psVar.referenceOfType(ImportedValueReference::class.java)
        val valueDeclaration = valueReference
            .multiResolve(false)
            .first().element as PsiNamedElement
        TestCase.assertEquals("y", valueDeclaration.name)
    }

    fun `test var can only resolve exported values`() {
        myFixture.addFileToProject(
            "Lib.purs",
            """
            module Lib where
            y = 1
            """.trimIndent()
        ) as PSFile
        val file = myFixture.addFileToProject(
            "Main.purs",
            """
            module Main where
            import Lib
            x = y
            """.trimIndent()
        ) as PSFile
        val psVar = file.getVarByName("y")!!
        val valueReference =
            psVar.referenceOfType(ImportedValueReference::class.java)
        val valueDeclarations = valueReference.multiResolve(false)
        TestCase.assertEquals(0, valueDeclarations.size)
    }

    fun `test var can't resolve imported values when hidden`() {
        myFixture.addFileToProject(
            "Lib.purs",
            """
            module Lib (y) where
            y = 1
            """.trimIndent()
        ) as PSFile
        val file = myFixture.addFileToProject(
            "Main.purs",
            """
            module Main where
            import Lib hiding (y)
            x = y
            """.trimIndent()
        ) as PSFile
        val psVar = file.getVarByName("y")!!
        val valueReference =
            psVar.referenceOfType(ImportedValueReference::class.java)
        val valueDeclarations = valueReference.multiResolve(false)
        TestCase.assertEquals(0, valueDeclarations.size)
    }

    fun `test var can resolve imported values when hiding others`() {
        myFixture.addFileToProject(
            "Lib.purs",
            """
            module Lib (y, z) where
            y = 1
            z = 2
            """.trimIndent()
        ) as PSFile
        val file = myFixture.addFileToProject(
            "Main.purs",
            """
            module Main where
            import Lib hiding (z)
            x = y
            """.trimIndent()
        ) as PSFile
        val psVar = file.getVarByName("y")!!
        val valueReference =
            psVar.referenceOfType(ImportedValueReference::class.java)
        val valueDeclarations = valueReference.multiResolve(false)
        TestCase.assertEquals(1, valueDeclarations.size)
    }

    fun `test var can only resolve imported named values when they are named`() {
        myFixture.addFileToProject(
            "Lib.purs",
            """
            module Lib (y, z) where
            y = 1
            z = 2
            """.trimIndent()
        ) as PSFile
        val file = myFixture.addFileToProject(
            "Main.purs",
            """
            module Main where
            import Lib (z)
            x = y
            """.trimIndent()
        ) as PSFile
        val psVar = file.getVarByName("y")!!
        val valueReference =
            psVar.referenceOfType(ImportedValueReference::class.java)
        val valueDeclarations = valueReference.multiResolve(false)
        TestCase.assertEquals(0, valueDeclarations.size)
    }

    fun `test var can resolve imported named values when they are named`() {
        myFixture.addFileToProject(
            "Lib.purs",
            """
            module Lib (y, z) where
            y = 1
            z = 2
            """.trimIndent()
        ) as PSFile
        val file = myFixture.addFileToProject(
            "Main.purs",
            """
            module Main where
            import Lib (y)
            x = y
            """.trimIndent()
        ) as PSFile
        val psVar = file.getVarByName("y")!!
        val valueReference =
            psVar.referenceOfType(ImportedValueReference::class.java)
        val valueDeclarations = valueReference.multiResolve(false)
        TestCase.assertEquals(1, valueDeclarations.size)
    }

    fun `test var can resolve to parameter`() {
        val file = myFixture.addFileToProject(
            "Main.purs",
            """
            module Main where
            x y = y
            """.trimIndent()
        ) as PSFile
        val psVar = file.getVarByName("y")!!
        val parameterReference =
            psVar.referenceOfType(ParameterReference::class.java)
        val identifier = parameterReference.resolve()!!
        TestCase.assertEquals("y", identifier.name)
    }

    fun `test var see all parameters`() {
        val file = myFixture.addFileToProject(
            "Main.purs",
            """
            module Main where
            x y z = y
            """.trimIndent()
        ) as PSFile
        val psVar = file.getVarByName("y")!!
        val parameterReference =
            psVar.referenceOfType(ParameterReference::class.java)
        val names = parameterReference.variants.map { it?.name }
        assertContainsElements(names, "z", "y")
    }

    private fun PsiElement.getVarByName(
        name: String
    ): PSVar? {
        return SyntaxTraverser
            .psiTraverser(this)
            .filterIsInstance(PSVar::class.java)
            .firstOrNull { it.text.trim() == name }
    }

    private fun <T> PsiElement.referenceOfType(
        referenceType: Class<T>
    ): T {
        return this
            .references
            .filterIsInstance(referenceType)
            .first()
    }
}
