package org.purescript.psi.declaration

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.purescript.getDataConstructor
import org.purescript.getDataDeclaration
import org.purescript.getValueDeclarations

class PSFixityDeclarationTest : BasePlatformTestCase() {

    fun `test rename`() {
        myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                import Foo ((+))
            """.trimIndent()
        )
        myFixture.configureByText(
            "Foo.purs",
            """
                module Foo ((+)) where
                falseAdd x y = x
                infixl 6 falseAdd as <caret>+
                x = 1 + 1
            """.trimIndent()
        )
        myFixture.renameElementAtCaret("++")
        myFixture.checkResult(
            "Foo.purs",
            """
                module Foo ((++)) where
                falseAdd x y = x
                infixl 6 falseAdd as ++
                x = 1 ++ 1
            """.trimIndent(),
            false
        )
        myFixture.checkResult(
            "Bar.purs",
            """
                module Bar where
                import Foo ((++))
            """.trimIndent(),
            false
        )
    }

    fun `test find corresponding value definition`() {
        val first = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                
                infix 0 <caret>foo as +++

                foo _ = 1
            """.trimIndent()
        ).getValueDeclarations().first()
        val reference = myFixture.getReferenceAtCaretPositionWithAssertion()
        assertEquals(first, reference.resolve())
    }

    fun `test find corresponding data definition`() {
        val first = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                
                infix 0 <caret>Foo as +++

                data Foo a = Foo a a
            """.trimIndent()
        ).getDataConstructor()
        val reference = myFixture.getReferenceAtCaretPositionWithAssertion()
        assertEquals(first, reference.resolve())
    }
    
    fun `test find imported corresponding data definition`() {
        val first = myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                
                data Bar a = Bar a a
            """.trimIndent()
        ).getDataConstructor()
        myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                
                import Bar (Bar(..))
                
                infix 0 <caret>Bar as +++

            """.trimIndent()
        )
        val reference = myFixture.getReferenceAtCaretPositionWithAssertion()
        assertEquals(first, reference.resolve())
    }
    fun `test find qualified imported corresponding data definition`() {
        val first = myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                
                data Bar a = Bar a a
            """.trimIndent()
        ).getDataConstructor()
        myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                
                import Bar (Bar(..)) as B
                
                infix 0 <caret>B.Bar as +++

            """.trimIndent()
        )
        val reference = myFixture.getReferenceAtCaretPositionWithAssertion()
        assertEquals(first, reference.resolve())
    }
}
