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
    }
}
