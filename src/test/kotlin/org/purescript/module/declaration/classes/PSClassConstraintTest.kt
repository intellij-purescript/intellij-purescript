package org.purescript.module.declaration.classes

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.purescript.getClassConstraint

class PSClassConstraintTest : BasePlatformTestCase() {

    fun `test parses constraint`() {
        val classConstraint = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                class Bar <= Qux
            """.trimIndent()
        ).getClassConstraint()

        assertEquals("Bar", classConstraint.name)
    }

    fun `test parses constraint with type atoms`() {
        val classConstraint = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                class Bar a b <= Qux a b c
            """.trimIndent()
        ).getClassConstraint()

        assertEquals("Bar", classConstraint.name)
    }
}
