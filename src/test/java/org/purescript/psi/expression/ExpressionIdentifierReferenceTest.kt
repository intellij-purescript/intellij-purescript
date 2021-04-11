package org.purescript.psi.expression

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.purescript.getExpressionIdentifier
import org.purescript.getValueDeclaration
import org.purescript.getValueDeclarations
import org.purescript.getVarBinder

class ExpressionIdentifierReferenceTest : BasePlatformTestCase() {

    // region value declarations

    fun `test resolves value declaration`() {
        val file = myFixture.configureByText(
            "Main.purs",
            """
                module Main where
                x = y
                y = 1
            """.trimIndent()
        )
        val expressionIdentifier = file.getExpressionIdentifier()
        val valueDeclaration = file.getValueDeclarations()[1]

        assertEquals(valueDeclaration, expressionIdentifier.reference.resolve())
    }

    fun `test resolves value declaration with multiple declarations`() {
        val file = myFixture.configureByText(
            "Main.purs",
            """
                module Main where
                x = y 1
                y 1 = 1
                y _ = 2
            """.trimIndent()
        )
        val valueDeclarations = file.getValueDeclarations()
        val expressionIdentifier = file.getExpressionIdentifier()

        assertTrue(expressionIdentifier.reference.isReferenceTo(valueDeclarations[1]))
        assertTrue(expressionIdentifier.reference.isReferenceTo(valueDeclarations[2]))
    }

    fun `test resolves imported value declarations`() {
        val valueDeclaration = myFixture.configureByText(
            "Lib.purs",
            """
                module Lib (y) where
                y = 1
            """.trimIndent()
        ).getValueDeclaration()
        val expressionIdentifier = myFixture.configureByText(
            "Main.purs",
            """
                module Main where
                import Lib
                x = y
            """.trimIndent()
        ).getExpressionIdentifier()

        assertEquals(valueDeclaration, expressionIdentifier.reference.resolve())
    }

    fun `test resolves deep imported value declarations`() {
        val valueDeclaration = myFixture.configureByText(
            "Y.purs",
            """
                module Y (y) where
                y = 1
            """.trimIndent()
        ).getValueDeclaration()
        myFixture.configureByText(
            "Lib.purs",
            """
                module Lib (module Y) where
                import Y
            """.trimIndent()
        )
        val expressionIdentifier = myFixture.configureByText(
            "Main.purs",
            """
                module Main where
                import Lib
                x = y
            """.trimIndent()
        ).getExpressionIdentifier()

        assertEquals(valueDeclaration, expressionIdentifier.reference.resolve())
    }

    fun `test does not resolve unexported value declarations`() {
        myFixture.configureByText(
            "Lib.purs",
            """
                module Lib (z) where
                y = 1
                z = 2
            """.trimIndent()
        )
        val expressionIdentifier = myFixture.configureByText(
            "Main.purs",
            """
                module Main where
                import Lib
                x = y
            """.trimIndent()
        ).getExpressionIdentifier()

        assertNull(expressionIdentifier.reference.resolve())
    }

    fun `test resolves value declaration exported using export all`() {
        val valueDeclaration = myFixture.configureByText(
            "Lib.purs",
            """
                module Lib where
                y = 1
            """.trimIndent()
        ).getValueDeclaration()
        val expressionIdentifier = myFixture.configureByText(
            "Main.purs",
            """
                module Main where
                import Lib
                x = y
            """.trimIndent()
        ).getExpressionIdentifier()

        assertEquals(valueDeclaration, expressionIdentifier.reference.resolve())
    }

    fun `test does not resolve hidden value declaration`() {
        myFixture.configureByText(
            "Lib.purs",
            """
                module Lib (y) where
                y = 1
            """.trimIndent()
        )
        val expressionIdentifier = myFixture.configureByText(
            "Main.purs",
            """
                module Main where
                import Lib hiding (y)
                x = y
            """.trimIndent()
        ).getExpressionIdentifier()

        assertNull(expressionIdentifier.reference.resolve())
    }

    fun `test resolves imported value declarations when hiding others`() {
        val valueDeclaration = myFixture.configureByText(
            "Lib.purs",
            """
                module Lib (y, z) where
                y = 1
                z = 2
            """.trimIndent()
        ).getValueDeclarations()[0]
        val expressionIdentifier = myFixture.configureByText(
            "Main.purs",
            """
                module Main where
                import Lib hiding (z)
                x = y
            """.trimIndent()
        ).getExpressionIdentifier()

        assertEquals(valueDeclaration, expressionIdentifier.reference.resolve())
    }

    fun `test does not resolve unimported value declarations`() {
        myFixture.configureByText(
            "Lib.purs",
            """
                module Lib (y, z) where
                y = 1
                z = 2
            """.trimIndent()
        )
        val expressionIdentifier = myFixture.configureByText(
            "Main.purs",
            """
                module Main where
                import Lib (z)
                x = y
            """.trimIndent()
        ).getExpressionIdentifier()

        assertNull(expressionIdentifier.reference.resolve())
    }

    fun `test resolves explicitly imported and exported value declarations`() {
        val valueDeclaration = myFixture.configureByText(
            "Lib.purs",
            """
                module Lib (y, z) where
                y = 1
                z = 2
            """.trimIndent()
        ).getValueDeclarations()[0]
        val expressionIdentifier = myFixture.configureByText(
            "Main.purs",
            """
                module Main where
                import Lib (y)
                x = y
            """.trimIndent()
        ).getExpressionIdentifier()

        assertEquals(valueDeclaration, expressionIdentifier.reference.resolve())
    }

    fun `test completes value declarations`() {
        myFixture.configureByText(
            "Main.purs",
            """
                module Main where
                import Foo
                import Bar hiding (y4)
                import Baz (y6)
                y0 = y<caret>
                y1 = 1
            """.trimIndent()
        )
        myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (y2) where
                y2 = 1
            """.trimIndent()
        )
        myFixture.configureByText(
            "Bar.purs",
            """
                module Bar (y4, y5) where
                y4 = 1
                y5 = 1
            """.trimIndent()
        )
        myFixture.configureByText(
            "Baz.purs",
            """
                module Baz (y6, y7) where
                y6 = 1
                y7 = 1
            """.trimIndent()
        )

        myFixture.testCompletionVariants("Main.purs", "y0", "y1", "y2", "y5", "y6")
    }

    // endregion

    // region var binders

    fun `test resolves var binders`() {
        val file = myFixture.configureByText(
            "Main.purs",
            """
                module Main where
                x y = y
            """.trimIndent()
        )
        val varBinder = file.getVarBinder()
        val expressionIdentifier = file.getExpressionIdentifier()

        assertEquals(varBinder, expressionIdentifier.reference.resolve())
    }

    fun `test resolves complex var binders`() {
        val file = myFixture.configureByText(
            "Main.purs",
            """
                module Main where
                x (Just y) = y
            """.trimIndent()
        )
        val varBinder = file.getVarBinder()
        val expressionIdentifier = file.getExpressionIdentifier()

        assertEquals(varBinder, expressionIdentifier.reference.resolve())
    }

    fun `test completes var binders`() {
        myFixture.configureByText(
            "Main.purs",
            """
                module Main where
                x y1 y2 = y<caret>
            """.trimIndent()
        )

        myFixture.testCompletionVariants("Main.purs", "y1", "y2")
    }

    // endregion

    // region foreign values
    // endregion
}
