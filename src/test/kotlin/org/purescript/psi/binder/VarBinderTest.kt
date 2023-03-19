package org.purescript.psi.binder

import com.intellij.testFramework.PsiTestUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class VarBinderTest : BasePlatformTestCase() {
    fun `test rename of parameter`() {
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
        PsiTestUtil.checkPsiStructureWithCommit(myFixture.file, PsiTestUtil::checkPsiMatchesTextIgnoringNonCode)

    }
}