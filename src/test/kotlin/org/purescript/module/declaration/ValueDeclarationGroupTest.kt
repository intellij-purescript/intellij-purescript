package org.purescript.module.declaration

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.purescript.getExpressionIdentifier
import org.purescript.getModule
import org.purescript.getValueDeclarationGroup
import org.purescript.getValueDeclarationGroupByName
import org.purescript.module.declaration.value.MoveValueDeclRefactoring
import org.purescript.module.declaration.value.ValueDeclarationGroup

class ValueDeclarationGroupTest : BasePlatformTestCase() {
    fun `xtest finds doc comment`() {
        val valueDeclaration = myFixture.addFileToProject(
            "Main.purs",
            """-- | This is
               -- | main module
               module Main (x, y) where
               -- | This is
               -- | main
               main = 1
            """.trimIndent()
        ).getValueDeclarationGroup()
        val docComments = valueDeclaration.docComments

        assertEquals(2, docComments.size)
        assertEquals("-- | This is", docComments[0].text)
        assertEquals("-- | main", docComments[1].text)
    }

    fun `xtest rename`() {
        myFixture.configureByText(
            "Main.purs",
            """
                module Main where
                
                import Foo (foo)
                
                x :: Int
                x = foo 0
            """.trimIndent()
        )
        myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (foo) where
                foo :: Int -> Int
                <caret>foo 0 = 1
                foo 1 = 2
                x = foo 0
            """.trimIndent()
        )
        myFixture.renameElementAtCaret("bar")
        myFixture.checkResult(
            "Foo.purs",
            """
                module Foo (bar) where
                bar :: Int -> Int
                bar 0 = 1
                bar 1 = 2
                x = bar 0
            """.trimIndent(),
            false
        )
        myFixture.checkResult(
            "Main.purs",
            """
                module Main where
                
                import Foo (bar)
                
                x :: Int
                x = bar 0
            """.trimIndent(),
            false
        )
    }

    fun `xtest move to module that imports it by name`() {
        val  main = myFixture.configureByText(
            "Main.purs",
            """
                module Main (class Box, x) where
                
                import Foo (foo)
                
                class Box a where
                    get :: a
                
                x :: Int
                x = foo 0
            """.trimIndent()
        ).getModule()
        val foo = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (foo) where
                
                import Numbers (one, (+))
                
                foo :: Int -> Int
                foo _ = one + one
                
                foz :: Int
                foz = foo 10
                
                fox :: Int
                fox = foo 10
            """.trimIndent()
        ).getValueDeclarationGroupByName("foo")
        myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                
                import Foo (foo)
                
                bar :: Int
                bar = foo 10
                baz :: Int
                baz = foo 10
            """.trimIndent()
        )
        myFixture.configureByText(
            "Numbers.purs",
            """
                module Numbers where
                
                infixl 6 add as +
                
                add a b = a
                
                one :: Int
                one = 1
            """.trimIndent()
        )
        
        MoveValueDeclRefactoring(foo, main).also {
            it.executeEx(it.findUsages())
        }
        
        myFixture.checkResult(
            "Foo.purs",
            """
                module Foo () where
                
                import Numbers (one, (+))
                import Main (foo)
                
                foz :: Int
                foz = foo 10
                
                fox :: Int
                fox = foo 10
            """.trimIndent(),
            false
        )
        myFixture.checkResult(
            "Main.purs",
            """
                module Main (class Box, x, foo) where
                
                import Numbers (one, (+))
                
                class Box a where
                    get :: a
                
                x :: Int
                x = foo 0
                
                foo :: Int -> Int
                foo _ = one + one
            """.trimIndent(),
            false
        )
        myFixture.checkResult(
            "Bar.purs",
            """
                module Bar where
                
                import Main (foo)
                
                bar :: Int
                bar = foo 10
                baz :: Int
                baz = foo 10
            """.trimIndent(),
            false
        )
    }
    
    fun `xtest move to module that imports it with alias`() {
        val  main = myFixture.configureByText(
            "Main.purs",
            """
                module Main (class Box, x) where
                
                import Foo (foo) as Foo
                
                class Box a where
                    get :: a
                
                x :: Int
                x = Foo.foo 0
            """.trimIndent()
        ).getModule()
        val foo = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (foo) where
                
                foo :: Int -> Int
                foo _ = 1
            """.trimIndent()
        ).getValueDeclarationGroup()
        myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                
                import Foo (foo) as Foo
                
                bar :: Int
                bar = Foo.foo 10
            """.trimIndent()
        )
        
        MoveValueDeclRefactoring(foo, main).also {
            it.executeEx(it.findUsages())
        }
        
        myFixture.checkResult(
            "Foo.purs",
            """
                module Foo () where
                
                
            """.trimIndent(),
            false
        )
        myFixture.checkResult(
            "Main.purs",
            """
                module Main (class Box, x, foo) where
                
                class Box a where
                    get :: a
                
                x :: Int
                x = foo 0
                
                foo :: Int -> Int
                foo _ = 1
            """.trimIndent(),
            false
        )

        myFixture.checkResult(
            "Bar.purs",
            """
                module Bar where
                
                import Main (foo) as Foo
                
                bar :: Int
                bar = Foo.foo 10
            """.trimIndent(),
            false
        )
    }

    fun `test importing module with alias hides values not prefixed with that alias`() {
        val f = myFixture.configureByText("Main.purs",
            """
                |module Main where
                |
                |import Alias as A
                |import Explicit (f)
                |
                |x = f
            """.trimMargin()
        ).getExpressionIdentifier()
        myFixture.configureByText("Alias.purs",
            """
                |module Alias where
                |
                |f = 10
            """.trimMargin()
        )
        myFixture.configureByText("Explicit.purs",
            """
                |module Explicit where
                |
                |f = 10
            """.trimMargin()
        )
        val reference = f.reference.resolve()
        TestCase.assertEquals("Explicit", (reference as? ValueDeclarationGroup)?.module?.name)
    }
}
