package org.purescript.psi.classes

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.purescript.getClassConstraint
import org.purescript.getClassDeclaration
import org.purescript.getClassDeclarations

class ClassConstraintReferenceTest : BasePlatformTestCase() {

    fun `test resolves class declaration in module`() {
        val classDeclarations = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                class Qux
                class Qux <= Bar
            """.trimIndent()
        ).getClassDeclarations()
        val superClassDeclaration = classDeclarations[0]
        val classConstraint = classDeclarations[1].classConstraints.single()

        assertEquals(superClassDeclaration, classConstraint.reference.resolve())
    }

    fun `test resolves imported class declaration`() {
        val superClassDeclaration = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                class Bar
            """.trimIndent()
        ).getClassDeclaration()
        val classConstraint = myFixture.configureByText(
            "Qux.purs",
            """
                module Qux where
                import Foo (class Bar)
                class Bar <= Qux
            """.trimIndent()
        ).getClassConstraint()

        assertEquals(superClassDeclaration, classConstraint.reference.resolve())
    }

    fun `test does not resolve non-existing class declaration`() {
        val classConstraint = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                class Qux <= Bar
            """.trimIndent()
        ).getClassConstraint()

        assertNull(classConstraint.reference.resolve())
    }

    fun `test does not resolve non-imported class declaration`() {
        myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                class Bar
            """.trimIndent()
        )
        val classConstraint = myFixture.configureByText(
            "Qux.purs",
            """
                module Qux where
                class Bar <= Qux
            """.trimIndent()
        ).getClassConstraint()

        assertNull(classConstraint.reference.resolve())
    }

    fun `test completes class declarations`() {
        myFixture.configureByText(
            "Bar.purs",
            """
                module Bar (class Bara, class Bira) where
                class Bara
                class Bira
                class Bar
            """.trimIndent()
        )
        myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar
                class Burk
                class B<caret> <= Foo
            """.trimIndent()
        )
        myFixture.testCompletionVariants("Foo.purs", "Bara", "Bira", "Burk")
    }

    fun `test finds usage of class declarations in other module`() {
        val classConstraint = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar
                class Qux <= Fum
            """.trimIndent()
        ).getClassConstraint()
        myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                class <caret>Qux
            """.trimIndent()
        )
        val usageInfo = myFixture.testFindUsages("Bar.purs").single()

        assertEquals(classConstraint, usageInfo.element)
    }

    fun `test finds usage of class declarations in same module`() {
        val classDeclarations = myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                class Qux <= Fum
                class <caret>Qux
            """.trimIndent()
        ).getClassDeclarations()
        val classConstraint = classDeclarations.first().classConstraints.single()
        val usageInfo = myFixture.testFindUsages("Bar.purs").single()

        assertEquals(classConstraint, usageInfo.element)
    }
}
