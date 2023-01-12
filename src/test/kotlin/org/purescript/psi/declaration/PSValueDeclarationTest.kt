package org.purescript.psi.declaration

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.purescript.getModule
import org.purescript.getValueDeclaration
import org.purescript.getValueDeclarationByName
import org.purescript.getValueDeclarations

class PSValueDeclarationTest : BasePlatformTestCase() {
    fun `test finds doc comment`() {
        val valueDeclaration = myFixture.addFileToProject(
            "Main.purs",
            """-- | This is
               -- | main module
               module Main (x, y) where
               -- | This is
               -- | main
               main = 1
            """.trimIndent()
        ).getValueDeclaration()
        val docComments = valueDeclaration.docComments

        assertEquals(2, docComments.size)
        assertEquals("-- | This is", docComments[0].text)
        assertEquals("-- | main", docComments[1].text)
    }

    fun `test rename`() {
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

    fun `test resolves sibling declarations`() {
        val first = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                foo 0 = 1
                <caret>foo 1 = 2
            """.trimIndent()
        ).getValueDeclarations().first()
        val reference = myFixture.getReferenceAtCaretPositionWithAssertion()
        assertEquals(first, reference.resolve())
    }

    fun `test move to module that imports it by name`() {
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
                
                foo :: Int -> Int
                foo _ = 1
                
                foz :: Int
                foz = foo 10
                
                fox :: Int
                fox = foo 10
            """.trimIndent()
        ).getValueDeclarationByName("foo")
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
        
        MoveValueDeclarationRefactoring(foo, main).also {
            it.executeEx(it.findUsages())
        }
        
        myFixture.checkResult(
            "Foo.purs",
            """
                module Foo () where
                
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
                
                import Main (foo)
                
                bar :: Int
                bar = foo 10
                baz :: Int
                baz = foo 10
            """.trimIndent(),
            false
        )
    }
    
    fun `test move to module that imports it with alias`() {
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
        ).getValueDeclaration()
        myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                
                import Foo (foo) as Foo
                
                bar :: Int
                bar = Foo.foo 10
            """.trimIndent()
        )
        
        MoveValueDeclarationRefactoring(foo, main).also {
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
}
