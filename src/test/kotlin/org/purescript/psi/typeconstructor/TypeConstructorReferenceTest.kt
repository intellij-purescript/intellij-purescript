package org.purescript.psi.typeconstructor

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.purescript.*

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

        assertEquals(dataDeclaration, typeConstructor.reference.resolve())
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

        assertEquals(typeConstructor, usageInfo.element)
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

        assertEquals(newtypeDeclaration, typeConstructor.reference.resolve())
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

        assertEquals(typeConstructor, usageInfo.element)
    }

    fun `test resolves local type synonym declarations`() {
        myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                type Bar a = a
                q :: Bar
            """.trimIndent()
        ).run {
            assertEquals(getTypeSynonymDeclaration(), getTypeConstructor().reference.resolve())
        }
    }

    fun `test resolves imported type synonym declarations`() {
        val typeSynonymDeclaration = myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                type Qux = Int
            """.trimIndent()
        ).getTypeSynonymDeclaration()
        val typeConstructor = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar
                a :: Qux
            """.trimIndent()
        ).getTypeConstructor()

        assertEquals(typeSynonymDeclaration, typeConstructor.reference.resolve())
    }
    
    fun `test resolves alias imported type synonym declarations`() {
        val typeSynonymDeclaration = myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                type Qux = Int
            """.trimIndent()
        ).getTypeSynonymDeclaration()
        val typeConstructor = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar as Bar
                a :: Bar.Qux
            """.trimIndent()
        ).getTypeConstructor()

        assertEquals(typeSynonymDeclaration, typeConstructor.reference.resolve())
    }

    fun `test completes type synonym declarations`() {
        myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                type Qux = Int
            """.trimIndent()
        )
        myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar
                type Bum = Int
                a :: <caret>
            """.trimIndent()
        )

        myFixture.testCompletionVariants("Foo.purs", "Qux", "Bum")
    }

    fun `test finds usages from local type synonym declarations`() {
        myFixture.configureByText(
            "Main.purs",
            """
                module Data where
                type B = Int
                type <caret>A = Int
                func :: A -> A
                func a = a
            """.trimIndent()
        )
        val usageInfo = myFixture.testFindUsages("Main.purs")

        assertEquals(2, usageInfo.size)
    }

    fun `test finds usages from imported type synonym declarations`() {
        val typeSynonymDeclaration = myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                type Qux = Int
            """.trimIndent()
        ).getTypeSynonymDeclaration()
        val typeConstructor = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar
                a :: Qux
            """.trimIndent()
        ).getTypeConstructor()
        val usageInfo = myFixture.findUsages(typeSynonymDeclaration).single()

        assertEquals(typeConstructor, usageInfo.element)
    }

    fun `test resolves local foreign data declarations`() {
        val file = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                foreign import data Bar :: Type
                q :: Bar
            """.trimIndent()
        )
        val foreignDataDeclaration = file.getForeignDataDeclaration()
        val typeConstructor = file.getTypeConstructors()[1]

        assertEquals("Bar", typeConstructor.name)
        assertEquals(foreignDataDeclaration, typeConstructor.reference.resolve())
    }

    fun `test resolves imported foreign data declarations`() {
        val foreignDataDeclaration = myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                foreign import data Qux :: Type
            """.trimIndent()
        ).getForeignDataDeclaration()
        val typeConstructor = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar
                a :: Qux
            """.trimIndent()
        ).getTypeConstructor()

        assertEquals(foreignDataDeclaration, typeConstructor.reference.resolve())
    }

    fun `test completes foreign data declarations`() {
        myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                foreign import data Qux :: Type
            """.trimIndent()
        )
        myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar
                foreign import data Bum :: Type
                a :: <caret>
            """.trimIndent()
        )

        myFixture.testCompletionVariants("Foo.purs", "Qux", "Bum")
    }

    fun `test finds usages from local foreign data declarations`() {
        myFixture.configureByText(
            "Main.purs",
            """
                module Data where
                foreign import data B :: Type
                foreign import data <caret>A :: Type
                func :: A -> A
                func a = a
            """.trimIndent()
        )
        val usageInfo = myFixture.testFindUsages("Main.purs")

        assertEquals(2, usageInfo.size)
    }

    fun `test finds usages from imported foreign data declarations`() {
        val foreignDataDeclaration = myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                foreign import data Qux :: Type
            """.trimIndent()
        ).getForeignDataDeclaration()
        val typeConstructor = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar
                a :: Qux
            """.trimIndent()
        ).getTypeConstructor()
        val usageInfo = myFixture.findUsages(foreignDataDeclaration).single()

        assertEquals(typeConstructor, usageInfo.element)
    }
}
