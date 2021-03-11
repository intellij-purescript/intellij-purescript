package org.purescript.psi

import com.intellij.psi.PsiFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.purescript.file.PSFile
import org.purescript.psi.imports.PSImportDeclarationImpl

class ModuleReferenceTest : BasePlatformTestCase() {

    private fun PsiFile.getModule(): PSModule =
        (this as PSFile).module

    private fun PsiFile.getImportDeclarations(): Array<PSImportDeclarationImpl> =
        getModule().importDeclarations

    fun `test resolves module`() {
        val module = myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
            """.trimIndent()
        ).getModule()
        val reference = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar
            """.trimIndent()
        ).getImportDeclarations().single().reference

        assertEquals(module, reference.resolve())
    }

    fun `test resolves aliased module`() {
        val module = myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
            """.trimIndent()
        ).getModule()
        val reference = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar as B
            """.trimIndent()
        ).getImportDeclarations().single().reference

        assertEquals(module, reference.resolve())
    }

    fun `test finds usage of module with doc comments`() {
        myFixture.configureByText(
            "Bar.purs",
            """
                -- | This is a doc comment
                module <caret>Bar where
            """.trimIndent()
        )
        val importDeclaration = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar
            """.trimIndent()
        ).getImportDeclarations().single()

        val usageInfo = myFixture.testFindUsages("Bar.purs").single()

        assertEquals(importDeclaration, usageInfo.element)
    }
}
