package org.purescript.psi.declaration

import com.intellij.testFramework.fixtures.BasePlatformTestCase

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
}
