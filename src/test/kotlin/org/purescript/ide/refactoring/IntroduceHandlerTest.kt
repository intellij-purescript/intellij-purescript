package org.purescript.ide.refactoring

import com.intellij.testFramework.PsiTestUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.intellij.lang.annotations.Language

class IntroduceHandlerTest : BasePlatformTestCase() {
    fun `test extract method in example`() {
        doTest(
            """
                module Main where

                import Prelude
                
                import Data.Array ((..))
                import Data.Foldable (for_)
                import Effect (Effect)
                import Effect.Console (logShow)
                
                
                main :: Effect Unit
                main = for_ (1 .. 100) {-caret-}logShow
            """.trimIndent(),
            """
                module Main where

                import Prelude
                
                import Data.Array ((..))
                import Data.Foldable (for_)
                import Effect (Effect)
                import Effect.Console (logShow)
                
                
                main :: Effect Unit
                main = for_ (1 .. 100) logShow'
                
                logShow' = logShow
            """.trimIndent()
        )
    }
    fun `it doesn't work to test extract all`() {
        doTest(
            """
                module Main where
                
                import Prelude
                
                x = 1
                
                y = {-caret-}x + x
            """.trimIndent(),
            """
                module Main where
                
                import Prelude
                
                x = 1
                
                y = foo + foo
                
                foo = x
                
            """.trimIndent()
        )
    }

    private fun doTest(
        @Language("Purescript") before: String,
        @Language("Purescript") after: String,
        filename: String = "Main.purs"
    ) {
        checkByText(before.trimIndent(), after.trimIndent(), filename) {
            myFixture.performEditorAction("ExtractMethod")
        }
    }

    protected fun checkByText(
        @Language("Purescript") before: String,
        @Language("Purescript") after: String,
        fileName: String = "Main.purs",
        action: () -> Unit
    ) {
        myFixture.configureByText(fileName, replaceCaretMarker(before))
        action()
        PsiTestUtil.checkPsiStructureWithCommit(
            myFixture.file,
            PsiTestUtil::checkPsiMatchesTextIgnoringNonCode
        )
        myFixture.checkResult(replaceCaretMarker(after))
    }

    protected fun replaceCaretMarker(text: String) =
        text.replace("{-caret-}", "<caret>")
}