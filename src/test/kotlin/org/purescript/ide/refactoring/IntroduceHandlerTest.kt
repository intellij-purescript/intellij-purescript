package org.purescript.ide.refactoring

import com.intellij.refactoring.util.CommonRefactoringUtil
import com.intellij.refactoring.util.CommonRefactoringUtil.RefactoringErrorHintException
import com.intellij.testFramework.PsiTestUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.intellij.lang.annotations.Language

class IntroduceHandlerTest : BasePlatformTestCase() {
    fun `test extract method of imported value`() {
        myFixture.configureByText("Console.purs", 
            """
                |module Lib where
                |
                |x = 1
            """.trimMargin()
            )
        doTest(
            """
                |module Main where
                |
                |import Lib (x)
                |
                |y = {-caret-}x
            """.trimMargin(),
            """
                |module Main where
                |
                |import Lib (x)
                |
                |y = x'
                |
                |x' = x
            """.trimMargin()
        )
    }
    fun `test creates parameters of identifiers not reachable from top level`() {
        doTest(
            """
            module Main where
            
            import Prelude
            
            y = {-caret-}x
              where x = 1
        """.trimIndent(),
            """
            module Main where
            
            import Prelude
            
            y = x' x
              where x = 1
            
            x' x = x
        """.trimIndent()
        )
    }
    fun `test it don't create parameter of identifiers declared inside expression`() {
        doTest(
            """
            |module Main where
            |
            |y = 
            |  {-caret-}let
            |    x = 1
            |  in x
        """.trimMargin(),
            """
            |module Main where
            |
            |y = 
            |  x'
            |
            |x' = let
            |    x = 1
            |  in x
        """.trimMargin()
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
                
                y = x' + x'
                
                x' = x
                
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