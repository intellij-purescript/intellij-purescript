package org.purescript.psi.`var`

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.purescript.file.PSFile
import org.purescript.psi.PSExportedModule

class ExportedModuleReferenceTest : BasePlatformTestCase() {

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
        val fileFoo = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (module Bar) where
                import Bar
            """.trimIndent()
        ) as PSFile
        val fileBar = myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
            """.trimIndent()
        ) as PSFile
        val exportedModule = fileFoo.module.exportList!!.exportedItems.single() as PSExportedModule

        TestCase.assertTrue(exportedModule.reference.isReferenceTo(fileBar.module))
    }

    fun `test does not resolve to module if not imported`() {
        val fileFoo = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (module Bar) where
            """.trimIndent()
        ) as PSFile
        val fileBar = myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
            """.trimIndent()
        ) as PSFile
        val exportedModule = fileFoo.module.exportList!!.exportedItems.single() as PSExportedModule

        TestCase.assertFalse(exportedModule.reference.isReferenceTo(fileBar.module))
    }

    fun `test does not resolve to module if it does not exist`() {
        val fileFoo = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (module Bar) where
            """.trimIndent()
        ) as PSFile
        val exportedModule = fileFoo.module.exportList!!.exportedItems.single() as PSExportedModule

        TestCase.assertTrue(exportedModule.reference.multiResolve(false).isEmpty())
    }
}
