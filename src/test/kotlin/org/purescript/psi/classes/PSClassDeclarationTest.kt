package org.purescript.psi.classes

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.purescript.getClassDeclaration

class PSClassDeclarationTest : BasePlatformTestCase() {

    fun `test parses empty class declaration`() {
        val classDeclaration = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                class Bar
            """.trimIndent()
        ).getClassDeclaration()

        assertEquals("Bar", classDeclaration.name)
        assertNull(classDeclaration.classConstraintList)
        assertNull(classDeclaration.functionalDependencyList)
        assertEmpty(classDeclaration.typeVarBindings)
        assertNull(classDeclaration.classMemberList)
    }

    fun `test parses empty class declaration with type var bindings`() {
        val classDeclaration = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                class Bar a b c
            """.trimIndent()
        ).getClassDeclaration()

        assertEquals("Bar", classDeclaration.name)
        assertNull(classDeclaration.classConstraintList)
        assertNull(classDeclaration.functionalDependencyList)
        assertSize(3, classDeclaration.typeVarBindings)
        assertNull(classDeclaration.classMemberList)
    }

    fun `test parses empty class with single constraint`() {
        val classDeclaration = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                class Qux b <= Bar b
            """.trimIndent()
        ).getClassDeclaration()

        assertEquals("Bar", classDeclaration.name)
        assertNotNull(classDeclaration.classConstraintList)
        assertNull(classDeclaration.functionalDependencyList)
        assertSize(1, classDeclaration.typeVarBindings)
        assertNull(classDeclaration.classMemberList)
    }

    fun `test parses empty class with multiple constraints`() {
        val classDeclaration = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                class (Qux a, Baz a b) <= Bar a b
            """.trimIndent()
        ).getClassDeclaration()

        assertEquals("Bar", classDeclaration.name)
        assertNotNull(classDeclaration.classConstraintList)
        assertNull(classDeclaration.functionalDependencyList)
        assertSize(2, classDeclaration.typeVarBindings)
        assertNull(classDeclaration.classMemberList)
    }

    fun `test parses empty class with functional dependencies`() {
        val classDeclaration = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                class Bar a b | a -> b, b -> a
            """.trimIndent()
        ).getClassDeclaration()

        assertEquals("Bar", classDeclaration.name)
        assertNull(classDeclaration.classConstraintList)
        assertNotNull(classDeclaration.functionalDependencyList)
        assertSize(2, classDeclaration.typeVarBindings)
        assertNull(classDeclaration.classMemberList)
    }

    fun `test parses class with class members`() {
        val classDeclaration = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                class Bar where
                    foo :: Int
                    bar :: String
            """.trimIndent()
        ).getClassDeclaration()

        assertEquals("Bar", classDeclaration.name)
        assertNull(classDeclaration.classConstraintList)
        assertNull(classDeclaration.functionalDependencyList)
        assertEmpty(classDeclaration.typeVarBindings)
        assertNotNull(classDeclaration.classMemberList)
    }

    fun `test parses class with everything`() {
        val classDeclaration = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                class (Qux a b, Muk c) <= Bar a b c | a -> b, c b -> a where
                    foo :: Int
                    bar :: String
            """.trimIndent()
        ).getClassDeclaration()

        assertEquals("Bar", classDeclaration.name)
        assertNotNull(classDeclaration.classConstraintList)
        assertSize(3, classDeclaration.typeVarBindings)
        assertNotNull(classDeclaration.functionalDependencyList)
        assertNotNull(classDeclaration.classMemberList)
    }

    fun `test parses class with kinded type variables`() {
        val classDeclaration = myFixture.configureByText(
            "Record.purs",
            """
                module Data.Codec.Argonaut.Record where
                -- | The class used to enable the building of `Record` codecs by providing a
                -- | record of codecs.
                class RowListCodec (rl ∷ RL.RowList) (ri ∷ Row Type) (ro ∷ Row Type) | rl → ri ro where
                    rowListCodec ∷ RLProxy rl → Record ri → CA.JPropCodec (Record ro)
            """.trimIndent()
        ).getClassDeclaration()

        assertEquals("RowListCodec", classDeclaration.name)
        assertNull(classDeclaration.classConstraintList)
        assertSize(3, classDeclaration.typeVarBindings)
        assertNotNull(classDeclaration.functionalDependencyList)
        assertNotNull(classDeclaration.classMemberList)
    }
}
