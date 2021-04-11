package org.purescript.psi.expression

import com.intellij.testFramework.UsefulTestCase
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.purescript.getImportDeclaration
import org.purescript.getImportDeclarations
import org.purescript.ide.inspections.PSUnresolvedReferenceInspection

class ImportExpressionConstructorQuickFixTest : BasePlatformTestCase() {

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

        assertEquals("Import", action.familyName)
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
        UsefulTestCase.assertSize(2, actions)
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
}
