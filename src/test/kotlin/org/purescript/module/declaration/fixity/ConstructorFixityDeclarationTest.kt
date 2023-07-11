package org.purescript.module.declaration.fixity

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.purescript.getDataConstructor

class ConstructorFixityDeclarationTest : BasePlatformTestCase() {

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
                data Tuple a b = Tuple a b
                infixl 6 Tuple as <caret>+
                x = 1 + 1
            """.trimIndent()
        )
        myFixture.renameElementAtCaret("++")
        myFixture.checkResult(
            "Foo.purs",
            """
                module Foo ((++)) where
                data Tuple a b = Tuple a b
                infixl 6 Tuple as ++
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
}
