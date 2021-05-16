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

    fun `test merges hiding and non-hiding imports`() {
        test(
            """
                module Foo where
                
                import Alphabet hiding (a)
                import Alphabet (a)
                
            """.trimIndent(),
            """
                module Foo where
                
                import Alphabet
                
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
                import Prelude (kind Kind)
                
            """.trimIndent(),
            """
                module Foo where
                
                import Prelude (class Applicative, kind Kind, type (~>), Unit, pure, (<*>))
                
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

    fun `test groups aliased imports when merging`() {
        test(
            """
                module Foo where
                
                import Bar (field) as B
                import Bar (class Class) as B
                import Bar (SomeType) as B
                import Bar (field)
                import Bar (class Class)
                import Bar (SomeType)
                import Bar (field) as A
                import Bar (class Class) as A
                import Bar (SomeType) as A
                
            """.trimIndent(),
            """
                module Foo where
                
                import Bar (class Class, SomeType, field)
                import Bar (class Class, SomeType, field) as A
                import Bar (class Class, SomeType, field) as B
                
            """.trimIndent()
        )
    }

    fun `test removes specific imports if one import imports all`() {
        test(
            """
                module Foo where
                
                import Bar (field) as B
                import Bar as B
                import Bar (SomeType) as B
                import Bar
                import Bar (class Class)
                import Bar (SomeType)
                
            """.trimIndent(),
            """
                module Foo where
                
                import Bar
                
                import Bar as B
                
            """.trimIndent()
        )
    }

    fun `test removes duplicate imports`() {
        test(
            """
                module Foo where
                
                import Prelude (pure)
                import Prelude ((<*>))
                import Prelude (type (~>))
                import Prelude (class Applicative)
                import Prelude (Unit)
                import Prelude (kind Kind)
                import Prelude (pure, pure)
                import Prelude ((<*>), (<*>))
                import Prelude (type (~>), type (~>))
                import Prelude (class Applicative, class Applicative)
                import Prelude (Unit, Unit)
                import Prelude (kind Kind, kind Kind)
                
            """.trimIndent(),
            """
                module Foo where
                
                import Prelude (class Applicative, kind Kind, type (~>), Unit, pure, (<*>))
            
            """.trimIndent()
        )
    }

    fun `test merges imported data`() {
        test(
            """
                module Foo where
                
                import Data.Maybe (Maybe)
                import Data.Maybe (Maybe(Just))
                import Data.Maybe (Maybe(Nothing))
                
            """.trimIndent(),
            """
                module Foo where
                
                import Data.Maybe (Maybe(Just, Nothing))
                
            """.trimIndent()
        )
    }

    fun `test merges imported data with double dot`() {
        test(
            """
                module Foo where
                
                import Data.Maybe (Maybe)
                import Data.Maybe (Maybe(Just))
                import Data.Maybe (Maybe(..))
                
            """.trimIndent(),
            """
                module Foo where
                
                import Data.Maybe (Maybe(..))
                
            """.trimIndent()
        )
    }

    fun `test merges imported data hiding items`() {
        test(
            """
                module Foo where
                
                import Bar hiding (a, b, c)
                import Bar hiding (b)
                import Bar hiding (b, c)
                
            """.trimIndent(),
            """
                module Foo where
                
                import Bar hiding (b)
                
            """.trimIndent()
        )
    }
}
