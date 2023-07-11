package org.purescript.module.binder

import com.intellij.testFramework.PsiTestUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class OperatorBinderTest : BasePlatformTestCase() {
    fun `test rename of parameter`() {
        myFixture.configureByText(
            "Foo.purs",
            """
                | module Foo ((++)) where
                | 
                | data Tuple a b = Tuple a b
                | 
                | infixl 6 Tuple as ++
                | 
                | first (a <caret>++ b) = a
                | 
                | monad = do
                |   a ++ b <- [1 ++ 2]
                |   [a]
            """.trimMargin()
        )
        myFixture.renameElementAtCaret("""/\""")
        myFixture.checkResult(
            """
                | module Foo ((/\)) where
                | 
                | data Tuple a b = Tuple a b
                | 
                | infixl 6 Tuple as /\
                | 
                | first (a <caret>/\ b) = a
                | 
                | monad = do
                |   a /\ b <- [1 /\ 2]
                |   [a]
            """.trimMargin()
        )
        PsiTestUtil.checkPsiStructureWithCommit(myFixture.file, PsiTestUtil::checkPsiMatchesTextIgnoringNonCode)
    }
}