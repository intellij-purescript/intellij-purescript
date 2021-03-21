package org.purescript.psi.classes

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
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

        TestCase.assertEquals("Bar", classDeclaration.name)
        TestCase.assertNull(classDeclaration.classConstraintList)
        TestCase.assertNull(classDeclaration.functionalDependencyList)
        assertEmpty(classDeclaration.typeVariables)
    }
}
