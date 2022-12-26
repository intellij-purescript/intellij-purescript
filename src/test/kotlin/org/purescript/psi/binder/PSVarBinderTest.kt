package org.purescript.psi.binder

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class PSVarBinderTest : BasePlatformTestCase() {
    fun `test rename of parameter` () {
            myFixture.configureByText(
                "Foo.purs",
                """
                module Foo where
                
                foo <caret>x = x
            """.trimIndent()
            )
            myFixture.renameElementAtCaret("y")
            myFixture.checkResult(
                """
                module Foo where
                
                foo y = y
            """.trimIndent()
            )

    }
}