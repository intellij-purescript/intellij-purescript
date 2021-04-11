package org.purescript.psi.expression

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.purescript.getExpressionIdentifier
import org.purescript.getValueDeclaration
import org.purescript.getValueDeclarations
import org.purescript.getVarBinder

class ExpressionIdentifierReferenceTest : BasePlatformTestCase() {

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
//        assertTrue(expressionIdentifier.reference.isReferenceTo(valueDeclarations[2]))
    }

//    fun `test var can see variants for all value declarations`() {
//        val file = myFixture.configureByText(
//            "Main.purs",
//            """
//                module Main where
//                import Foo
//                import Bar hiding (w)
//                import Baz (k)
//                x = y
//                y = 1
//            """.trimIndent()
//        )
//        myFixture.configureByText(
//            "Foo.purs",
//            """
//                module Foo (z) where
//                z = 1
//            """.trimIndent()
//        )
//        myFixture.configureByText(
//            "Bar.purs",
//            """
//                module Bar (w, q) where
//                w = 1
//                q = 1
//            """.trimIndent()
//        )
//        myFixture.configureByText(
//            "Baz.purs",
//            """
//                module Baz (k, p) where
//                k = 1
//                p = 1
//            """.trimIndent()
//        )
//        val expressionIdentifier = file.getExpressionIdentifier()
//        val variants = expressionIdentifier.reference.variants
//        assertContainsElements(variants, "z", "q", "k")
//        assertDoesntContain(variants, "w", "p")
//    }

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

//    fun `test var see all parameters`() {
//        myFixture.configureByText(
//            "Main.purs",
//            """
//                module Main where
//                x y z = y
//            """.trimIndent()
//        )
//        val psVar = file.getVarByName("y")!!
//        val parameterReference =
//            psVar.referenceOfType(ParameterReference::class.java)
//        val names = parameterReference.variants.map { it?.name }
//        assertContainsElements(names, "z", "y")
//    }

}
