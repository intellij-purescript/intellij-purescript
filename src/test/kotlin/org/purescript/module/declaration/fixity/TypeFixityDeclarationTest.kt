package org.purescript.module.declaration.fixity

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.purescript.getDataDeclaration
import org.purescript.getTypeSynonymDeclaration

class TypeFixityDeclarationTest : BasePlatformTestCase() {

    fun `ignore test rename`() {
        myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                import Foo (type (+))
            """.trimIndent()
        )
        myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (type (+)) where
                data Tuple a b = a b
                infixl 6 type Tuple as <caret>+
                infixl 6 Tuple as +
                x :: Int + Int
                x = 1 + 1
            """.trimIndent()
        )
        myFixture.renameElementAtCaret("++")
        myFixture.checkResult(
            "Foo.purs",
            """
                module Foo (type (++)) where
                data Tuple a b = a b
                infixl 6 type Tuple as <caret>++
                infixl 6 Tuple as +
                x :: Int ++ Int
                x = 1 + 1
            """.trimIndent(),
            false
        )
        myFixture.checkResult(
            "Bar.purs",
            """
                module Bar where
                import Foo (type (++))
            """.trimIndent(),
            false
        )
    }

    fun `test find corresponding data declaration`() {
        val first = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                
                infix 0 type <caret>Foo as +++

                data Foo a b = Foo a b
            """.trimIndent()
        ).getDataDeclaration()
        val reference = myFixture.getReferenceAtCaretPositionWithAssertion()
        assertEquals(first, reference.resolve())
    }

    fun `test find corresponding imported data declaration`() {
        val first = myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                
                data Bar a b = Bar a b
            """.trimIndent()
        ).getDataDeclaration()
        myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                
                import Bar (Bar)
                
                infix 0 type <caret>Bar as +++

            """.trimIndent()
        )
        val reference = myFixture.getReferenceAtCaretPositionWithAssertion()
        assertEquals(first, reference.resolve())
    }
    
    fun `test find corresponding imported type alias`() {
        val first = myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                
                type Bar a b = Baz a b
                data Baz a b = Baz a b
            """.trimIndent()
        ).getTypeSynonymDeclaration()
        myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                
                import Bar (Bar)
                
                infix 0 type <caret>Bar as +++

            """.trimIndent()
        )
        val reference = myFixture.getReferenceAtCaretPositionWithAssertion()
        assertEquals(first, reference.resolve())
    }
    
    fun `test find corresponding type alias`() {
        val first = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                
                infix 0 type <caret>Bar as +++
                type Bar a b = Baz a b
                data Baz a b = Baz a b
            """.trimIndent()
        ).getTypeSynonymDeclaration()
        val reference = myFixture.getReferenceAtCaretPositionWithAssertion()
        assertEquals(first, reference.resolve())
    }
}
