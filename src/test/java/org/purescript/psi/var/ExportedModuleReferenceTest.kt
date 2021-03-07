package org.purescript.psi.`var`

import com.intellij.psi.PsiFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.purescript.file.PSFile
import org.purescript.psi.PSExportedModule
import org.purescript.psi.PSModule

class ExportedModuleReferenceTest : BasePlatformTestCase() {

    private fun PsiFile.getModule(): PSModule =
        (this as PSFile).module

    private fun PsiFile.getExportedModule(): PSExportedModule =
        getModule().exportList!!.exportedItems.single() as PSExportedModule

    fun `test completes imported modules`() {
        myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (module <caret>) where
                import Prelude
                import Data.String
            """.trimIndent()
        )
        myFixture.testCompletionVariants("Foo.purs", "Prelude", "Data.String")
    }

    fun `test completes aliased import declaration`() {
        myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (module <caret>) where
                import Prelude as P
                import Data.String as DS
            """.trimIndent()
        )
        myFixture.testCompletionVariants("Foo.purs", "P", "DS")
    }

    fun `test resolves to imported module`() {
        val exportedModule = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (module Bar) where
                import Bar
            """.trimIndent()
        ).getExportedModule()
        val module = myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
            """.trimIndent()
        ).getModule()

        TestCase.assertTrue(exportedModule.reference.isReferenceTo(module))
    }

    fun `test resolves to aliased imported module`() {
        val exportedModule = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (module B) where
                import Bar as B
            """.trimIndent()
        ).getExportedModule()
        val module = myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
            """.trimIndent()
        ).getModule()

        TestCase.assertTrue(exportedModule.reference.isReferenceTo(module))
    }

    fun `test does not resolve to module if not imported`() {
        val exportedModule = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (module Bar) where
            """.trimIndent()
        ).getExportedModule()
        val module = myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
            """.trimIndent()
        ).getModule()

        TestCase.assertFalse(exportedModule.reference.isReferenceTo(module))
    }

    fun `test does not resolve to aliased module using wrong name`() {
        val exportedModule = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (module Bar) where
                import Bar as B
            """.trimIndent()
        ).getExportedModule()
        val module = myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
            """.trimIndent()
        ).getModule()

        TestCase.assertFalse(exportedModule.reference.isReferenceTo(module))
    }

    fun `test does not resolve to module if it does not exist`() {
        val exportedModule = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (module Bar) where
            """.trimIndent()
        ).getExportedModule()

        TestCase.assertTrue(exportedModule.reference.multiResolve(false).isEmpty())
    }
}
