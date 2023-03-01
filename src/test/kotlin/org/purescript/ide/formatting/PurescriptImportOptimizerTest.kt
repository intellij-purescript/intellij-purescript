package org.purescript.ide.formatting

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase

class PurescriptImportOptimizerTest : BasePlatformTestCase() {

    private fun test(fileContentBefore: String, expectedFileContentAfter: String) {
        val file = myFixture.configureByText("Foo.purs", fileContentBefore)
        val processFile = PurescriptImportOptimizer().processFile(file)
        WriteCommandAction.runWriteCommandAction(myFixture.project, processFile)
        TestCase.assertEquals(expectedFileContentAfter, file.text)
    }

    fun `test does nothing when no imports`() {
        test(
            "module Foo where",
            """module Foo where
                |
            """.trimMargin()
        )
    }

    fun `test add empty line between where and first declaration`() {
        test(
            """module Foo where
                |f = 3
            """.trimMargin(),
            """module Foo where
                |
                |f = 3
            """.trimMargin()
        )
    }

    fun `test removes redundant empty lines`() {
        test(
            """module Foo where
                |
                |
                |f = 3
            """.trimMargin(),
            """module Foo where
                |
                |f = 3
            """.trimMargin()
        )
    }

    fun `test whitespace formatting with declaration and import`() {
        test(
            """module Foo where
                |import Prelude
                |f = 3
            """.trimMargin(),
            """module Foo where
                |
                |import Prelude
                |
                |f = 3
            """.trimMargin()
        )
    }

    fun `test whitespace formatting with declaration and import with too many whitespace`() {
        test(
            """module Foo where
                |import Prelude
                |
                |
                |
                |f = 3
            """.trimMargin(),
            """module Foo where
                |
                |import Prelude
                |
                |f = 3
            """.trimMargin()
        )
    }

    fun `test preserves whitespace`() {
        test(
            """
                module Foo where

                import A
                import B
                import C

            """.trimIndent(),
            """
                module Foo where

                import A
                import B
                import C

            """.trimIndent(),
        )
    }

    fun `test sorts according to module name`() {
        test(
            """
                module Foo where
                import Giraffe
                import Pig
                import Dog
                import Cat
            """.trimIndent(),
            """
                module Foo where
                
                import Cat
                import Dog
                import Giraffe
                import Pig
                
            """.trimIndent()
        )
    }

    fun `test removes unused imports`() {
        myFixture.configureByText(
            "Bar.purs",
            """
                |module Bar where
                |data Bar = Bar
            """.trimMargin()
        )
        test(
            """
                |module Foo where
                |
                |import Bar (Bar(Bar))
            """.trimMargin(),
            """
                |module Foo where
                |
            """.trimMargin()
        )
    }
}
