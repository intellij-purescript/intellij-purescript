package org.purescript.ide.refactoring

import com.intellij.testFramework.PsiTestUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.intellij.lang.annotations.Language

class PSInlineTest : BasePlatformTestCase() {
    fun `test deletes inlined declaration`() {
        doTest(
            """
                module Main where
                x{-caret-} = 1
                y = 1
            """.trimIndent(),
            """
                module Main where
                y = 1
            """.trimIndent()
        )
    }

    fun `test inline value declaration with single value and no parameters`() {
        doTest(
            """
                module Main where
                x = 1
                y = {-caret-}x
            """.trimIndent(),
            """
                module Main where
                y = 1
            """.trimIndent()
        )
    }

    fun `test inline value declaration with expression and no parameters`() {
        doTest(
            """
                module Main where
                f a = a
                x = f 1
                y = {-caret-}x
            """.trimIndent(),
            """
                module Main where
                f a = a
                y = f 1
            """.trimIndent()
        )
    }

    fun `test inline value declaration with expression and no parameters using parentesis if usages is not single value`() {
        doTest(
            """
                module Main where
                f a = a
                x = f 1
                y = f {-caret-}x
            """.trimIndent(),
            """
                module Main where
                f a = a
                y = f (f 1)
            """.trimIndent()
        )
    }

    private fun doTest(
        @Language("Purescript") before: String,
        @Language("Purescript") after: String,
        filename: String = "Main.purs"
    ) {
        checkByText(before.trimIndent(), after.trimIndent(), filename) {
            myFixture.performEditorAction("Inline")
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