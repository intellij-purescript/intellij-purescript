package org.purescript.module.exports

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.purescript.getExportedModule
import org.purescript.getImportDeclaration
import org.purescript.getModule

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

        assertTrue(exportedModule.reference.isReferenceTo(module))
    }

    fun `test aliased exported module resolves to import declaration`() {
        val file = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (module B) where
                import Bar as B
            """.trimIndent()
        )
        val exportedModule = file.getExportedModule()
        val importAlias = file.getImportDeclaration().importAlias!!

        assertTrue(exportedModule.reference.isReferenceTo(importAlias))
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

        assertFalse(exportedModule.reference.isReferenceTo(module))
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

        assertFalse(exportedModule.reference.isReferenceTo(module))
    }

    fun `test does not resolve to module if it does not exist`() {
        val exportedModule = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (module Bar) where
            """.trimIndent()
        ).getExportedModule()

        assertNull(exportedModule.reference.resolve())
    }

    fun `test finds usage of import alias`() {
        val file = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (module B) where
                import Bar as B
            """.trimIndent()
        )
        val exportedModule = file.getExportedModule()
        val importAlias = file.getImportDeclaration().importAlias!!
        val usageInfo = myFixture.findUsages(importAlias).single()

        assertEquals(exportedModule, usageInfo.element)
    }

    fun `test renames module`() {
        val module = myFixture.configureByText("Foo.purs", "module Foo where").getModule()
        val exportedModule = myFixture.configureByText(
            "Bar.purs",
            """
                module Bar (module <caret>Foo) where
                import Foo
            """.trimIndent()
        ).getExportedModule()
        myFixture.renameElementAtCaret("Qux")

        assertEquals("Qux", exportedModule.name)
        assertEquals("Qux", module.name)
    }
}
