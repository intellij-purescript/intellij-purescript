package org.purescript.module.declaration.fixity

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.purescript.getValueDeclarationGroupByName

class FixityDeclarationTest : BasePlatformTestCase() {

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
}
