package org.purescript.psi.declaration

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.purescript.getValueDeclaration

class SignatureReferenceTest : BasePlatformTestCase() {
    fun `test resolves value declarations`() {
        val valueDeclaration = myFixture.configureByText(
            "Foo.purs", """
            module Foo where
            <caret>foo :: Int
            foo = 42
        """.trimIndent()
        ).getValueDeclaration()

        val reference = myFixture.getReferenceAtCaretPositionWithAssertion()

        TestCase.assertEquals(valueDeclaration, reference.resolve())
    }
}