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


    fun `test sorts according to alias`() {
        test(
            """
                module Foo where
                
                import Alphabet as C
                import Alphabet as A
                import Alphabet as B
                
            """.trimIndent(),
            """
                module Foo where
                
                import Alphabet as A
                import Alphabet as B
                import Alphabet as C
                
            """.trimIndent()
        )
    }


    fun `test merges imports and sorts import items by type`() {
        test(
            """
                module Foo where
                
                import Prelude (pure)
                import Prelude ((<*>))
                import Prelude (type (~>))
                import Prelude (class Applicative)
                import Prelude (Unit)
                
            """.trimIndent(),
            """
                module Foo where
                
                import Prelude (class Applicative, type (~>), Unit, pure, (<*>))
                
            """.trimIndent()
        )
    }

    fun `test sorts import items by name`() {
        test(
            """
                module Foo where
                
                import Prelude (pure, map)
                
            """.trimIndent(),
            """
                module Foo where
                
                import Prelude (map, pure)
                
            """.trimIndent()
        )
    }
}
