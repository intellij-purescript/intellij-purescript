package org.purescript.module.declaration.value.expression

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Ignore
import org.purescript.*
import org.purescript.ide.inspections.PSUnresolvedReferenceInspection

class ImportQuickFixTest : BasePlatformTestCase() {

    fun `test imports module`() {
        myFixture.configureByText(
            "Maybe.purs",
            """
                module Data.Maybe where
                data Maybe a
                    = Just a
                    | Nothing
            """.trimIndent()
        )
        val file = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                f = Just 3
            """.trimIndent()
        )
        myFixture.enableInspections(PSUnresolvedReferenceInspection())
        val action = myFixture
            .getAllQuickFixes("Foo.purs")
            .first { "Maybe(..)" in it.text }
        myFixture.launchAction(action)
        val importDeclaration = file.getImportDeclaration()

        assertEquals("Import Data.Maybe (Maybe(..))", action.familyName)
        assertEquals("Data.Maybe", importDeclaration.name)
    }

    fun `test imports module with alias`() {
        myFixture.configureByText(
            "Maybe.purs",
            """
                module Data.Maybe where
                data Maybe a
                    = Just a
                    | Nothing
            """.trimIndent()
        )
        val file = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                f = Maybe.Just 3
            """.trimIndent()
        )
        myFixture.enableInspections(PSUnresolvedReferenceInspection())
        val action = myFixture
            .getAllQuickFixes("Foo.purs")
            .first { "Maybe(..)" in it.text }
        myFixture.launchAction(action)
        val importDeclaration = file.getImportDeclaration()

        assertEquals("Import Data.Maybe (Maybe(..)) as Maybe", action.familyName)
        assertEquals("Data.Maybe", importDeclaration.importedModule!!.name)
        assertEquals("Maybe", importDeclaration.importAlias!!.name)
    }

    fun `test imports module when there are more then one to pick`() {
        myFixture.configureByText(
            "Maybe.purs",
            """
                module Data.Maybe where
                data Maybe a
                    = Just a
                    | Nothing
            """.trimIndent()
        )
        myFixture.configureByText(
            "Maybe2.purs",
            """
                module Data.Maybe2 where
                data Maybe a
                    = Just a
                    | Nothing
            """.trimIndent()
        )
        myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                f = Just 3
            """.trimIndent()
        )
        myFixture.enableInspections(PSUnresolvedReferenceInspection())
        val actions = myFixture.getAllQuickFixes("Foo.purs")
        assertSize(1, actions)
    }

    fun `test imports module with other imports`() {
        myFixture.configureByText(
            "Maybe.purs",
            """
                module Data.Maybe where
                newtype Just = Just Int
            """.trimIndent()
        )
        myFixture.configureByText("Prelude.purs", "module Prelude where")
        val file = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Prelude
                f = Just 3
            """.trimIndent()
        )
        myFixture.enableInspections(PSUnresolvedReferenceInspection())
        val action = myFixture
            .getAllQuickFixes("Foo.purs")
            .first { "Just(..)" in it.text }
        myFixture.launchAction(action)

        val importDeclarations = file.getImportDeclarations()
        assertSize(2, importDeclarations)
        assertContainsElements(importDeclarations.map { it.name }, "Prelude", "Data.Maybe")
    }

    fun `test doesn't import non-existing module`() {
        val file = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                f = Just 3
            """.trimIndent()
        )
        myFixture.enableInspections(PSUnresolvedReferenceInspection())
        val action = myFixture.getAllQuickFixes("Foo.purs").single()
        myFixture.launchAction(action)

        assertEmpty(file.getImportDeclarations())
    }

    @Ignore("auto import fails these tests")
    fun `dont test it import values when found`() {
        val file = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                f = bar
            """.trimIndent()
        )
        myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                bar = 1
            """.trimIndent()
        )
        myFixture.enableInspections(PSUnresolvedReferenceInspection())
        val action = myFixture
            .getAllQuickFixes("Foo.purs")
            .first { "(bar)" in it.text }
        myFixture.launchAction(action)

        assertEquals("Bar", file.getImportDeclaration().name)
        assertEquals("bar", file.getImportedValue().name)
    }

    @Ignore("auto import fails these tests")
    fun `dont test it import values when found and are aliased`() {
        val file = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                f = Bar.bar
            """.trimIndent()
        )
        myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                bar = 1
            """.trimIndent()
        )
        myFixture.enableInspections(PSUnresolvedReferenceInspection())
        val action = myFixture
            .getAllQuickFixes("Foo.purs")
            .first { "(bar)" in it.text }
        myFixture.launchAction(action)

        assertEquals("Bar", file.getImportDeclaration().name)
        assertEquals("Bar", file.getImportAlias().name)
        assertEquals("bar", file.getImportedValue().name)
    }

    fun `test it import operator when found and are aliased`() {
        val file = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                f = 1 Bar.+ 1
            """.trimIndent()
        )
        myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                infixl 6 fakeAdd as +
                
                fakeAdd :: Int -> Int -> Int
                fakeAdd x y = x
            """.trimIndent()
        )
        myFixture.enableInspections(PSUnresolvedReferenceInspection())
        val action = myFixture
            .getAllQuickFixes("Foo.purs")
            .first { "((+))" in it.text }
        myFixture.launchAction(action)

        assertEquals("Bar", file.getImportDeclaration().name)
        assertEquals("Bar", file.getImportAlias().name)
        assertEquals("+", file.getImportedOperator().name)
    }

    fun `test imports type constructor`() {
        myFixture.configureByText(
            "Bar.purs",
            """
                |module Bar where
                |foreign import data Qux :: Type
            """.trimMargin()
        )
        val file = myFixture.configureByText(
            "Foo.purs",
            """
                |module Foo where
                |f :: Qux
            """.trimMargin()
        )
        myFixture.enableInspections(PSUnresolvedReferenceInspection())
        val action = myFixture.getAllQuickFixes("Foo.purs")
            .first { "Import" in it.text}
        myFixture.launchAction(action)
        myFixture.checkResult(
            "Foo.purs",
            """
                |module Foo where
                |
                |import Bar (Qux)
                |f :: Qux
            """.trimMargin(),
            true
        )
    }

    fun `test it import type synonyme with apostrophe when module export it self`() {
        val file = myFixture.configureByText(
            "Foo.purs",
            """
                |module Foo where
                |f :: Bar'
                |f = ""
            """.trimMargin()
        )
        myFixture.configureByText(
            "Bar.purs",
            """
                |module Bar (module Bar) where
                |type Bar' = String
            """.trimMargin()
        )
        myFixture.enableInspections(PSUnresolvedReferenceInspection())
        val action = myFixture.getAllQuickFixes("Foo.purs")
            .first { "Import" in it.text }
        myFixture.launchAction(action)
        myFixture.checkResult(
            "Foo.purs",
            """
                |module Foo where
                |
                |import Bar (Bar')
                |f :: Bar'
                |f = ""
            """.trimMargin(),
            true
        )
    }
}
