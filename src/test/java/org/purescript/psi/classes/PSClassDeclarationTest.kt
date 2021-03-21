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
}
