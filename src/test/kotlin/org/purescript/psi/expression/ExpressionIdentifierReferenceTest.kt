package org.purescript.psi.expression

import com.intellij.psi.stubs.StubIndex
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.indexing.DumbModeAccessType
import com.intellij.util.indexing.DumbModeAccessType.RELIABLE_DATA_ONLY
import org.purescript.*

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

        assertEquals(valueDeclaration, expressionIdentifier.getReference().resolve())
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
        @Suppress("UnstableApiUsage")
        val resolve = expressionIdentifier.getReference().resolve()
        assertEquals(valueDeclaration, resolve)
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

        assertEquals(valueDeclaration, expressionIdentifier.getReference().resolve())
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

        assertNull(expressionIdentifier.getReference().resolve())
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

        assertEquals(valueDeclaration, expressionIdentifier.getReference().resolve())
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

        assertNull(expressionIdentifier.getReference().resolve())
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

        assertEquals(valueDeclaration, expressionIdentifier.getReference().resolve())
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

        assertNull(expressionIdentifier.getReference().resolve())
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

        assertEquals(valueDeclaration, expressionIdentifier.getReference().resolve())
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

        myFixture.testCompletionVariants("Main.purs", "y0", "y1", "y2", "y5", "y6", "y4", "y7")
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

        assertEquals(varBinder, expressionIdentifier.getReference().resolve())
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

        assertEquals(varBinder, expressionIdentifier.getReference().resolve())
    }

    fun `test resolves record var binders`() {
        val file = myFixture.configureByText(
            "Main.purs",
            """
                module Main where
                x {y} = y
            """.trimIndent()
        )
        val varBinder = file.getVarBinder()
        val expressionIdentifier = file.getExpressionIdentifier()

        assertEquals(varBinder, expressionIdentifier.getReference().resolve())
    }

    fun `test resolves var binders used in record expressions`() {
        val file = myFixture.configureByText(
            "Main.purs",
            """
                module Main where
                x y = {y}
            """.trimIndent()
        )
        val varBinder = file.getVarBinder()
        val expressionIdentifier = file.getExpressionIdentifier()

        assertEquals(varBinder, expressionIdentifier.getReference().resolve())
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

    fun `test resolves foreign value declarations`() {
        val file = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                foreign import x :: Int
                y = x
            """.trimIndent()
        )
        val expressionIdentifier = file.getExpressionIdentifier()
        val foreignValueDeclaration = file.getForeignValueDeclaration()

        assertEquals(foreignValueDeclaration, expressionIdentifier.getReference()
            .resolve())
    }

    fun `test completes foreign value declarations`() {
        myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                foreign import qux :: Int
                foreign import qut :: Int
            """.trimIndent()
        )
        myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar
                y = q<caret>
            """.trimIndent()
        )

        myFixture.testCompletionVariants("Foo.purs", "qux", "qut")
    }

    // endregion

    // region qualified

    fun `test completes qualified values first`() {
        myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                foreign import y1 :: Int
                foreign import y2 :: Int
            """.trimIndent()
        )
        myFixture.configureByText(
            "Quz.purs",
            """
                module Quz where
                foreign import y3 :: Int
                y4 = 4
            """.trimIndent()
        )
        myFixture.configureByText(
            "Baz.purs",
            """
                module Baz (y5, module B) where
                import Bar hiding (y1) as B
                y5 :: Int
                y5 = 5
                foreign import y6 :: Int
            """.trimIndent()
        )
        myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar
                import Quz as Q
                import Baz as B
                y0 = B.y<caret>
            """.trimIndent()
        )

        myFixture.testCompletionVariants("Foo.purs", "y2", "y4", "y5")
    }

    // endregion
}
