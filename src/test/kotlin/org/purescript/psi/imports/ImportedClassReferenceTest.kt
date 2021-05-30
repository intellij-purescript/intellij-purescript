package org.purescript.psi.imports

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.purescript.getClassDeclaration
import org.purescript.getImportedClass

class ImportedClassReferenceTest : BasePlatformTestCase() {

    fun `test resolves class declaration`() {
        val classDeclaration = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                class Bar
            """.trimIndent()
        ).getClassDeclaration()
        val importedClass = myFixture.configureByText(
            "Qux.purs",
            """
                module Qux where
                import Foo (class Bar)
            """.trimIndent()
        ).getImportedClass()

        assertEquals(classDeclaration, importedClass.reference.resolve())
    }

    fun `test does not resolve non-existing class declaration`() {
        myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                class Bar
            """.trimIndent()
        )
        val importedClass = myFixture.configureByText(
            "Qux.purs",
            """
                module Qux where
                import Foo (class Baz)
            """.trimIndent()
        ).getImportedClass()

        assertNull(importedClass.reference.resolve())
    }

    fun `test does not resolve non-exported class declaration`() {
        myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (Car) where
                class Bar
                class Car
            """.trimIndent()
        )
        val importedClass = myFixture.configureByText(
            "Qux.purs",
            """
                module Qux where
                import Foo (class Bar)
            """.trimIndent()
        ).getImportedClass()

        assertNull(importedClass.reference.resolve())
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
                import Bar (class B<caret>)
            """.trimIndent()
        )
        myFixture.testCompletionVariants("Foo.purs", "Bara", "Bira")
    }

    fun `test finds usage of class declarations`() {
        val importedClass = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar (class Qux)
            """.trimIndent()
        ).getImportedClass()
        myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                class <caret>Qux
            """.trimIndent()
        )
        val usageInfo = myFixture.testFindUsages("Bar.purs").single()

        assertEquals(importedClass, usageInfo.element)
    }
}
