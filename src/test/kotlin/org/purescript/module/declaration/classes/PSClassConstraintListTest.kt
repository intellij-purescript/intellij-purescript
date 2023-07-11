package org.purescript.module.declaration.classes

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.purescript.getClassDeclaration

class PSClassConstraintListTest : BasePlatformTestCase() {

    fun `test parses class constraint list with single constraint`() {
        val classConstraintList = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                class Qux <= Bar
            """.trimIndent()
        ).getClassDeclaration().classConstraintList!!

        assertSize(1, classConstraintList.classConstraints)
    }

    fun `test parses class constraint list with multiple constraints`() {
        val classConstraintList = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                class (Qux, Baz, Bur) <= Bar
            """.trimIndent()
        ).getClassDeclaration().classConstraintList!!

        assertSize(3, classConstraintList.classConstraints)
    }

    fun `test parses class constraint list with type variables`() {
        val classConstraintList = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                class (Qux a, Baz b c) <= Bar a b c
            """.trimIndent()
        ).getClassDeclaration().classConstraintList!!

        assertSize(2, classConstraintList.classConstraints)
    }

    fun `test parses class constraint list with single constraint and type variables`() {
        val classConstraintList = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                class Qux a b c <= Bar a b c
            """.trimIndent()
        ).getClassDeclaration().classConstraintList!!

        assertSize(1, classConstraintList.classConstraints)
    }
}
