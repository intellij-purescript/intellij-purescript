package org.purescript.psi.declaration

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.purescript.getValueDeclaration
import org.purescript.getValueDeclarations

class PSValueDeclarationTest : BasePlatformTestCase() {
    fun `test finds doc comment`() {
        val valueDeclaration = myFixture.addFileToProject(
            "Main.purs",
            """-- | This is
               -- | main module
               module Main (x, y) where
               -- | This is
               -- | main
               main = 1
            """.trimIndent()
        ).getValueDeclaration()
        val docComments = valueDeclaration.docComments

        assertEquals(2, docComments.size)
        assertEquals("-- | This is", docComments[0].text)
        assertEquals("-- | main", docComments[1].text)
    }

    fun `test rename`() {
        myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                <caret>foo 0 = 1
                foo 1 = 2
                x = foo 0
            """.trimIndent()
        )
        myFixture.renameElementAtCaret("bar")
        myFixture.checkResult(
            """
                module Foo where
                bar 0 = 1
                bar 1 = 2
                x = bar 0
            """.trimIndent()
        )
    }

    fun `test resolves sibling declarations`() {
        val first = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                foo 0 = 1
                <caret>foo 1 = 2
            """.trimIndent()
        ).getValueDeclarations().first()
        val reference = myFixture.getReferenceAtCaretPositionWithAssertion()
        TestCase.assertEquals(first, reference.resolve())
    }
}
