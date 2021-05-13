package org.purescript.psi.expression

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.purescript.getImportDeclaration
import org.purescript.getImportDeclarations
import org.purescript.getImportedData
import org.purescript.getImportedValue
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
        val action = myFixture.getAllQuickFixes("Foo.purs").single()
        myFixture.launchAction(action)
        val importDeclaration = file.getImportDeclaration()

        assertEquals("Import Data.Maybe", action.familyName)
        assertEquals("Data.Maybe", importDeclaration.name)
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
        val file = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                f = Just 3
            """.trimIndent()
        )
        myFixture.enableInspections(PSUnresolvedReferenceInspection())
        val actions = myFixture.getAllQuickFixes("Foo.purs")
        assertSize(2, actions)
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
        val action = myFixture.getAllQuickFixes("Foo.purs").single()
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

    fun `test it import values when found`() {
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
        val action = myFixture.getAllQuickFixes("Foo.purs").single()
        myFixture.launchAction(action)

        assertEquals("Bar", file.getImportDeclaration().name)
        assertEquals("bar", file.getImportedValue().name)
    }

    fun `test imports type constructor`() {
        myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                foreign import data Qux :: Type
            """.trimIndent()
        )
        val file = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                f :: Qux
            """.trimIndent()
        )
        myFixture.enableInspections(PSUnresolvedReferenceInspection())
        val action = myFixture.getAllQuickFixes("Foo.purs").single()
        myFixture.launchAction(action)
        val importDeclaration = file.getImportDeclaration()
        val importedData = file.getImportedData()

        assertEquals("Import Bar (Qux)", action.familyName)
        assertEquals("Bar", importDeclaration.name)
        assertEquals("Qux", importedData.name)
    }
}
