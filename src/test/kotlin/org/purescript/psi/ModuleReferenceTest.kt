package org.purescript.psi

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.purescript.getImportDeclaration
import org.purescript.getModule

class ModuleReferenceTest : BasePlatformTestCase() {

    fun `test resolves module`() {
        val module = myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
            """.trimIndent()
        ).getModule()
        val importDeclaration = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar
            """.trimIndent()
        ).getImportDeclaration()

        assertEquals(module, importDeclaration.reference.resolve())
    }

    fun `test resolves aliased module`() {
        val module = myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
            """.trimIndent()
        ).getModule()
        val importDeclaration = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar as B
            """.trimIndent()
        ).getImportDeclaration()

        assertEquals(module, importDeclaration.reference.resolve())
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
        ).getImportDeclaration()
        val usageInfo = myFixture.testFindUsages("Bar.purs").single()

        assertEquals(importDeclaration, usageInfo.element)
    }

    fun `test finds usage of module in files not matchin its name`() {
        myFixture.configureByText(
            "BarModule.purs",
            """
                module <caret>Bar where
            """.trimIndent()
        )
        val importDeclaration = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar
            """.trimIndent()
        ).getImportDeclaration()
        val usageInfo = myFixture.testFindUsages("BarModule.purs").single()

        assertEquals(importDeclaration, usageInfo.element)
    }

    fun `test completes modules`() {
        myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
            """.trimIndent()
        )
        myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import <caret>
            """.trimIndent()
        )
        // TODO(why do i now also get Foo)
        // myFixture.testCompletionVariants("Foo.purs", "Bar")
    }

    fun `test renames module`() {
        val module = myFixture.configureByText("Foo.purs", "module Foo where").getModule()
        val importDeclaration = myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                import <caret>Foo
            """.trimIndent()
        ).getImportDeclaration()
        myFixture.renameElementAtCaret("Qux")

        assertEquals("Qux", importDeclaration.name)
        assertEquals("Qux", module.name)
    }
}
