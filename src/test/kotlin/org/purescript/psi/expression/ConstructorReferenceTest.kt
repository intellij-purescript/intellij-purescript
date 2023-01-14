package org.purescript.psi.expression

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.purescript.getDataConstructor
import org.purescript.getDataDeclaration
import org.purescript.getExpressionConstructor
import org.purescript.getNewTypeConstructor

class ConstructorReferenceTest : BasePlatformTestCase() {
    fun `test resolves local data declaration constructors`() {
        val file = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                data Bar = Bar1 | Bar2 Int
                f = Bar2 3
            """.trimIndent()
        )
        val dataDeclaration = file.getDataDeclaration()
        val dataConstructor = dataDeclaration.dataConstructors[1]
        val expressionConstructor = file.getExpressionConstructor()

        assertEquals(dataConstructor, expressionConstructor.getReference().resolve())
    }

    fun `test resolves imported data declaration constructors`() {
        val dataDeclaration = myFixture.configureByText(
            "Hup.purs",
            """
                module Hup where
                data Bar = Bar1 | Bar2 Int
            """.trimIndent()
        ).getDataDeclaration()
        val expressionConstructor = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Hup
                f = Bar2 3
            """.trimIndent()
        ).getExpressionConstructor()
        val dataConstructor = dataDeclaration.dataConstructors[1]

        assertEquals(dataConstructor, expressionConstructor.getReference().resolve())
    }

    fun `test completes data declaration constructors`() {
        myFixture.configureByText(
            "Hup.purs",
            """
                module Hup where
                data Bar = Bar1 | Bar2 Int
            """.trimIndent()
        )
        myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Hup
                data Hum = Bar3
                f = <caret>
            """.trimIndent()
        )

        myFixture.testCompletionVariants("Foo.purs", "Bar1", "Bar2", "Bar3")
    }

    fun `test finds usages of local data declaration constructors`() {
        val dataConstructor = myFixture.configureByText(
            "Hup.purs",
            """
                module Hup where
                data Bar = Qux
            """.trimIndent()
        ).getDataConstructor()
        val expressionConstructor = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Hup
                f = Qux
            """.trimIndent()
        ).getExpressionConstructor()
        val usageInfo = myFixture.findUsages(dataConstructor).single()

        assertEquals(expressionConstructor, usageInfo.element)
    }

    fun `test resolves local newtype declaration constructors`() {
        val file = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                newtype Bar = Qux Int
                f = Qux 3
            """.trimIndent()
        )
        val newTypeConstructor = file.getNewTypeConstructor()
        val expressionConstructor = file.getExpressionConstructor()

        assertEquals(newTypeConstructor, expressionConstructor.getReference().resolve())
    }

    fun `test resolves imported newtype declaration constructors`() {
        val newTypeConstructor = myFixture.configureByText(
            "Hup.purs",
            """
                module Hup where
                newtype Bar = Bar2 Int
            """.trimIndent()
        ).getNewTypeConstructor()
        val expressionConstructor = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Hup
                f = Bar2 3
            """.trimIndent()
        ).getExpressionConstructor()

        assertEquals(newTypeConstructor, expressionConstructor.getReference().resolve())
    }

    fun `test completes newtype declaration constructors`() {
        myFixture.configureByText(
            "Hup.purs",
            """
                module Hup where
                newtype Bar = Bar2 Int
            """.trimIndent()
        )
        myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Hup
                newtype Hum = Bar3 String
                f = <caret>
            """.trimIndent()
        )

        myFixture.testCompletionVariants("Foo.purs", "Bar2", "Bar3")
    }

    fun `test finds usages of imported newtype declaration constructors`() {
        val newTypeConstructor = myFixture.configureByText(
            "Hup.purs",
            """
                module Hup where
                newtype Bar = Qux String
            """.trimIndent()
        ).getNewTypeConstructor()
        val expressionConstructor = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Hup
                f = Qux ""
            """.trimIndent()
        ).getExpressionConstructor()
        val usageInfo = myFixture.findUsages(newTypeConstructor).single()

        assertEquals(expressionConstructor, usageInfo.element)
    }

    fun `test does not resolve imported newtype constructor when constructor not exported`() {
        myFixture.configureByText(
            "Hup.purs",
            """
                module Hup (Bar) where
                newtype Bar = Qux String
            """.trimIndent()
        )
        val expressionConstructor = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Hup
                f = Qux ""
            """.trimIndent()
        ).getExpressionConstructor()

        assertNull(expressionConstructor.getReference().resolve())
    }

    fun `test does not resolve imported newtype constructor when constructor not imported`() {
        myFixture.configureByText(
            "Hup.purs",
            """
                module Hup where
                newtype Bar = Qux String
            """.trimIndent()
        )
        val expressionConstructor = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Hup (Bar)
                f = Qux ""
            """.trimIndent()
        ).getExpressionConstructor()

        assertNull(expressionConstructor.getReference().resolve())
    }

    fun `test does not resolve imported data constructor when constructor not exported`() {
        myFixture.configureByText(
            "Hup.purs",
            """
                module Hup (Bar(Baz)) where
                data Bar = Qux String | Baz
            """.trimIndent()
        )
        val expressionConstructor = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Hup
                f = Qux ""
            """.trimIndent()
        ).getExpressionConstructor()

        assertNull(expressionConstructor.getReference().resolve())
    }

    fun `test does not resolve imported data constructor when constructor not imported`() {
        myFixture.configureByText(
            "Hup.purs",
            """
                module Hup where
                data Bar = Qux String | Baz
            """.trimIndent()
        )
        val expressionConstructor = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Hup (Baz)
                f = Qux ""
            """.trimIndent()
        ).getExpressionConstructor()

        assertNull(expressionConstructor.getReference().resolve())
    }

    fun `test does not resolve imported data constructor when constructor is hidden`() {
        myFixture.configureByText(
            "Hup.purs",
            """
                module Hup where
                data Bar = Qux String | Baz
            """.trimIndent()
        )
        val expressionConstructor = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Hup hiding (Bar(..))
                f = Qux ""
            """.trimIndent()
        ).getExpressionConstructor()

        assertNull(expressionConstructor.getReference().resolve())
    }

    fun `test resolves imported data constructor with explicitly imported constructor`() {
        val dataConstructor = myFixture.configureByText(
            "Maybe.purs",
            """
                module Data.Maybe where
                data Maybe a = Just a
            """.trimIndent()
        ).getDataConstructor()
        val expressionConstructor = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Data.Maybe (Maybe(Just))
                f = Just 3
            """.trimIndent()
        ).getExpressionConstructor()

        assertEquals(dataConstructor, expressionConstructor.getReference().resolve())
    }

    fun `test resolves alias imported data declaration constructors`() {
        val dataDeclaration = myFixture.configureByText(
            "Hup.purs",
            """
                module Hup where
                data Bar = Bar1 | Bar2 Int
            """.trimIndent()
        ).getDataDeclaration()
        val expressionConstructor = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Hup as H
                f = H.Bar2 3
            """.trimIndent()
        ).getExpressionConstructor()
        val dataConstructor = dataDeclaration.dataConstructors[1]

        assertEquals(dataConstructor, expressionConstructor.getReference().resolve())
    }

    fun `test resolves alias imported newtype declaration constructors`() {
        val newTypeConstructor = myFixture.configureByText(
            "Hup.purs",
            """
                module Hup where
                newtype Bar = Bar3 Int
            """.trimIndent()
        ).getNewTypeConstructor()
        val expressionConstructor = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Hup as H
                f = H.Bar3 3
            """.trimIndent()
        ).getExpressionConstructor()

        assertEquals(newTypeConstructor, expressionConstructor.getReference().resolve())
    }

    fun `test does not resolve when missing qualifier`() {
        myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                data B = C
            """.trimIndent()
        )
        val expressionConstructor = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar as Qux
                f = C
            """.trimIndent()
        ).getExpressionConstructor()

        assertNull(expressionConstructor.getReference().resolve())
    }

    fun `test does not resolve when wrong qualifier`() {
        myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                data B = C
            """.trimIndent()
        )
        val expressionConstructor = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar as Qux
                f = Bar.C
            """.trimIndent()
        ).getExpressionConstructor()

        assertNull(expressionConstructor.getReference().resolve())
    }

    fun `test does not resolve locally when qualifier`() {
        val expressionConstructor = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                data B = C
                f = Bar.C
            """.trimIndent()
        ).getExpressionConstructor()

        assertNull(expressionConstructor.getReference().resolve())
    }

    fun `test completes with qualifier`() {
        myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                data B = Bal | Bap | Bak
            """.trimIndent()
        )
        myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar as Qux
                newtype C = Baw Int
                f = Qux.<caret>
            """.trimIndent()
        )
        myFixture.testCompletionVariants("Foo.purs", "Bal", "Bap", "Bak")
    }
}
