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

    fun `test deletes inlined type declaration`() {
        doTest(
            """
                module Main where
                x :: Int
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
                x{-caret-} = 1
                y = x
            """.trimIndent(),
            """
                module Main where
                y = (1)
            """.trimIndent()
        )
    }

    fun `test inline value declaration with expression and no parameters`() {
        doTest(
            """
                module Main where
                f a = a
                x{-caret-} = f 1
                y = x
            """.trimIndent(),
            """
                module Main where
                f a = a
                y = (f 1)
            """.trimIndent()
        )
    }

    fun `test inline value declaration with expression and no parameters using parentesis if usages is not single value`() {
        doTest(
            """
                module Main where
                f a = a
                x{-caret-} = f 1
                y = f x
            """.trimIndent(),
            """
                module Main where
                f a = a
                y = f (f 1)
            """.trimIndent()
        )
    }
    
    fun `test inline value from where`() {
        doTest(
            """
                |module Main where
                |f a = a
                |y = f x
                |  where
                |    x{-caret-} = f 1
            """.trimMargin(),
            """
                |module Main where
                |f a = a
                |y = f (f 1)
                |  
                |
            """.trimMargin()
        )
    }    
    fun `test inline value from do let`() {
        doTest(
            """
                |module Main where
                |f a = a
                |y = do
                |  let
                |    x{-caret-} = f 1
                |  pure $ f x
            """.trimMargin(),
            """
                |module Main where
                |f a = a
                |y = do
                |  pure $ f (f 1)
            """.trimMargin()
        )
    }
    fun `xtest inline value from let in`() {
        doTest(
            """
                |module Main where
                |f a = a
                |y =
                |  let
                |    x{-caret-} = f 1
                |  in f x
            """.trimMargin(),
            """
                |module Main where
                |f a = a
                |y =
                |  f (f 1)
            """.trimMargin()
        )
    }
    
    fun `xtest inline simple value from let in`() {
        doTest(
            """
                |module Main where
                |y =
                |  let
                |    x{-caret-} = 1
                |  in x
            """.trimMargin(),
            """
                |module Main where
                |y = (1)
            """.trimMargin()
        )
    }
    fun `test inline value to record pun`() {
        doTest(
            """
                |module Main where
                |x{-caret-} = 1
                |y = {x}
            """.trimMargin(),
            """
                |module Main where
                |y = {x: 1}
            """.trimMargin()
        )
    }
    fun `test inline value to record`() {
        doTest(
            """
                |module Main where
                |x{-caret-} = 1
                |y = {z: x}
            """.trimMargin(),
            """
                |module Main where
                |y = {z: 1}
            """.trimMargin()
        )
    }
    
    fun `test inline with parameter`() {
        doTest(
            """
                |module Main where
                |x{-caret-} n = 1 + n
                |y = x 2
            """.trimMargin(),
            """
                |module Main where
                |y = (1 + 2)
            """.trimMargin()
        )
    }
    
    fun `test function with parameter in call site with no arguments`() {
        doTest(
            """
                |module Main where
                |x{-caret-} n = 1 + n
                |y = x
            """.trimMargin(),
            """
                |module Main where
                |y = (\n -> 1 + n)
            """.trimMargin()
        )
    }
    
    fun `test function with parameter in call site with fewer arguments`() {
        doTest(
            """
                |module Main where
                |x{-caret-} n c = 1 + n + c
                |y = x 1
            """.trimMargin(),
            """
                |module Main where
                |y = (\c -> 1 + 1 + c)
            """.trimMargin()
        )
    }
    fun `test operator`() {
        doTest(
            """
                |module Main where
                |
                |add a b = a
                |
                |infix 0 add as +
                |
                |y a b = a {-caret-}+ b
            """.trimMargin(),
            """
                |module Main where
                |
                |add a b = a
                |
                |infix 0 add as +
                |
                |y a b = (add a b)
            """.trimMargin()
        )
    }
    fun `test operator as argument`() {
        doTest(
            """
                |module Main where
                |
                |add a b = a
                |
                |f x = x
                |
                |infix 0 add as +
                |
                |y a b = f (a {-caret-}+ b)
            """.trimMargin(),
            """
                |module Main where
                |
                |add a b = a
                |
                |f x = x
                |
                |infix 0 add as +
                |
                |y a b = f ((add a b))
            """.trimMargin()
        )
    }
    fun `test operator as call`() {
        doTest(
            """
                |module Main where
                |
                |add a b = a
                |
                |f x = x
                |
                |infix 0 add as +
                |
                |y a b = (a {-caret-}+ b) f
            """.trimMargin(),
            """
                |module Main where
                |
                |add a b = a
                |
                |f x = x
                |
                |infix 0 add as +
                |
                |y a b = ((add a b)) f
            """.trimMargin()
        )
    }
    fun `test it dont inline operator as part of larger expression`() {
        doTest(
            """
                |module Main where
                |
                |add a b = a
                |
                |infixl 0 add as +
                |
                |y a b c = a {-caret-}+ b + c
            """.trimMargin(),
            """
                |module Main where
                |
                |add a b = a
                |
                |infixl 0 add as +
                |
                |y a b c = a {-caret-}+ b + c
            """.trimMargin()
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