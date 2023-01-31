package org.purescript.psi.declaration

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.purescript.*

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
        ).getValueDeclarationGroupByName("foo")
        val reference = myFixture.getReferenceAtCaretPositionWithAssertion()
        assertEquals(first, reference.resolve())
    }

    fun `test find corresponding data constructor`() {
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
    
    fun `test find imported corresponding data constructor`() {
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
    fun `test find qualified imported corresponding data constructor`() {
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
