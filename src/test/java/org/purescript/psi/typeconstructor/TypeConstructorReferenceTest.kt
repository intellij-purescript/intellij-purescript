package org.purescript.psi.typeconstructor

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.purescript.getDataDeclaration
import org.purescript.getNewTypeDeclaration
import org.purescript.getTypeConstructor

class TypeConstructorReferenceTest : BasePlatformTestCase() {

    fun `test resolves local data declarations`() {
        myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                data Bar
                q :: Bar
            """.trimIndent()
        ).run {
            assertEquals(getDataDeclaration(), getTypeConstructor().reference.resolve())
        }
    }

    fun `test resolves imported data declarations`() {
        val dataDeclaration = myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                data Qux
            """.trimIndent()
        ).getDataDeclaration()
        val typeConstructor = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar
                a :: Qux
            """.trimIndent()
        ).getTypeConstructor()

        TestCase.assertEquals(dataDeclaration, typeConstructor.reference.resolve())
    }

    fun `test completes data declarations`() {
        myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                data Qux
            """.trimIndent()
        )
        myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar
                data Bum
                a :: <caret>
            """.trimIndent()
        )

        myFixture.testCompletionVariants("Foo.purs", "Qux", "Bum")
    }

    fun `test finds usages from local data declarations`() {
        myFixture.configureByText(
            "Main.purs",
            """
                module Data where
                data B = B
                data <caret>A = A
                func :: A -> A
                func a = a
                """.trimIndent()
        )
        val usageInfo = myFixture.testFindUsages("Main.purs")
        assertEquals(2, usageInfo.size)
    }

    fun `test finds usages from imported data declarations`() {
        val dataDeclaration = myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                data Qux
            """.trimIndent()
        ).getDataDeclaration()
        val typeConstructor = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar
                a :: Qux
            """.trimIndent()
        ).getTypeConstructor()
        val usageInfo = myFixture.findUsages(dataDeclaration).single()

        TestCase.assertEquals(typeConstructor, usageInfo.element)
    }

    fun `test resolves local newtype declarations`() {
        myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                newtype Bar = Bar a
                q :: Bar
            """.trimIndent()
        ).run {
            assertEquals(getNewTypeDeclaration(), getTypeConstructor().reference.resolve())
        }
    }

    fun `test resolves imported newtype declarations`() {
        val newtypeDeclaration = myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                newtype Qux = Qux a
            """.trimIndent()
        ).getNewTypeDeclaration()
        val typeConstructor = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar
                a :: Qux
            """.trimIndent()
        ).getTypeConstructor()

        TestCase.assertEquals(newtypeDeclaration, typeConstructor.reference.resolve())
    }

    fun `test completes newtype declarations`() {
        myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                newtype Qux = Qux a
            """.trimIndent()
        )
        myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar
                newtype Bum = Bum a
                a :: <caret>
            """.trimIndent()
        )

        myFixture.testCompletionVariants("Foo.purs", "Qux", "Bum")
    }

    fun `test finds usages from local newtype declarations`() {
        myFixture.configureByText(
            "Main.purs",
            """
                module Data where
                newtype B = B a
                newtype <caret>A = A a
                func :: A -> A
                func a = a
                """.trimIndent()
        )
        val usageInfo = myFixture.testFindUsages("Main.purs")
        assertEquals(2, usageInfo.size)
    }

    fun `test finds usages from imported newtype declarations`() {
        val newtypeDeclaration = myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                newtype Qux = Qux a
            """.trimIndent()
        ).getNewTypeDeclaration()
        val typeConstructor = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar
                a :: Qux
            """.trimIndent()
        ).getTypeConstructor()
        val usageInfo = myFixture.findUsages(newtypeDeclaration).single()

        TestCase.assertEquals(typeConstructor, usageInfo.element)
    }
}
