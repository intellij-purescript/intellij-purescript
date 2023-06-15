package org.purescript.typechecker

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.purescript.getValueDeclarationGroupByName

class TypeCheckerIntegrationTest : BasePlatformTestCase() {

    fun `test it knows what type simple literals has`() {
        val file = myFixture.configureByText(
            "Main.purs",
            """
                |module Main where
                |
                |int = 42
                |string = "hello world"
                |bool = true
            """.trimMargin()
        )
        val int = file.getValueDeclarationGroupByName("int")
        val string = file.getValueDeclarationGroupByName("string")
        val bool = file.getValueDeclarationGroupByName("bool")
        TestCase.assertEquals("Prim.Int", int.checkType().toString())
        TestCase.assertEquals("Prim.String", string.checkType().toString())
        TestCase.assertEquals("Prim.Boolean", bool.checkType().toString())
    }

    fun `test it follows identifier references`() {
        val file = myFixture.configureByText(
            "Main.purs",
            """
                |module Main where
                |
                |int = 42
                |f = int
            """.trimMargin()
        )
        val f = file.getValueDeclarationGroupByName("f")
        TestCase.assertEquals("Prim.Int", f.checkType().toString())
    }

    fun `test it uses type annotations on value groups`() {
        val file = myFixture.configureByText(
            "Main.purs",
            """
                |module Main where
                |
                |f :: String -> Boolean -> Int
                |f x y = 42
            """.trimMargin()
        )
        val f = file.getValueDeclarationGroupByName("f")
        TestCase.assertEquals(
            "Prim.String -> Prim.Boolean -> Prim.Int",
            f.checkType().toString()
        )
    }
    
    fun `test it applies argument to functions of primitive type`() {
        val file = myFixture.configureByText(
            "Main.purs",
            """
                |module Main where
                |
                |f :: Int -> Int
                |f y = y + 42
                |
                |x = f 10
            """.trimMargin()
        )
        val x = file.getValueDeclarationGroupByName("x")
        TestCase.assertEquals("Prim.Int", x.checkType().toString())
    }
    
    fun `test it applies arguments to functions of primitive type`() {
        val file = myFixture.configureByText(
            "Main.purs",
            """
                |module Main where
                |
                |f :: Int -> Int -> Int
                |f y z = y + 42 + z
                |
                |x = f 10 42
            """.trimMargin()
        )
        val x = file.getValueDeclarationGroupByName("x")
        TestCase.assertEquals("Prim.Int", x.checkType().toString())
    }


    fun `test it applies arguments to functions of simple for all`() {
        val file = myFixture.configureByText(
            "Main.purs",
            """
                |module Main where
                |
                |f :: forall a. a -> a
                |f y = y
                |
                |x = f 10
            """.trimMargin()
        )
        val x = file.getValueDeclarationGroupByName("x")
        TestCase.assertEquals("Prim.Int", x.checkType().toString())
        val file2 = myFixture.configureByText(
            "Main.purs",
            """
                |module Main where
                |
                |f :: forall a. a -> a
                |f y = y
                |
                |x = f "Hello World"
            """.trimMargin()
        )
        val x2 = file2.getValueDeclarationGroupByName("x")
        TestCase.assertEquals("Prim.String", x2.checkType().toString())
    }
    fun `test it applies arguments to functions of simple for all with primitive return type`() {
        val file = myFixture.configureByText(
            "Main.purs",
            """
                |module Main where
                |
                |f :: forall a. a -> String
                |f y = "Hello World"
                |
                |x = f 10
            """.trimMargin()
        )
        val x = file.getValueDeclarationGroupByName("x")
        TestCase.assertEquals("Prim.String", x.checkType().toString())

        val file2 = myFixture.configureByText(
            "Main.purs",
            """
                |module Main where
                |
                |f :: forall a. a -> Int
                |f _ = 42
                |
                |x = f 10
            """.trimMargin()
        )
        val x2 = file2.getValueDeclarationGroupByName("x")
        TestCase.assertEquals("Prim.Int", x2.checkType().toString())
    }
    fun `xtest it applies arguments to functions of forall with two variables`() {
        val file = myFixture.configureByText(
            "Main.purs",
            """
                |module Main where
                |
                |f :: forall a b. a -> b -> a
                |f a _ = a
                |
                |x = f 10 "Hello"
            """.trimMargin()
        )
        val x = file.getValueDeclarationGroupByName("x")
        TestCase.assertEquals("Prim.Int", x.checkType().toString())
    }

    fun `test it rembers type aliases`() {
        val file = myFixture.configureByText(
            "Main.purs",
            """
                |module Main where
                |
                |type Truth = Int
                |
                |truth :: Truth
                |truth = 42
                |
                |f = truth
            """.trimMargin()
        )
        val f = file.getValueDeclarationGroupByName("f")
        TestCase.assertEquals("Main.Truth", f.checkType().toString())
    }
    
    fun `test it types single argument`() {
        val file = myFixture.configureByText(
            "Main.purs",
            """
                |module Main where
                |
                |id a = a
                |
            """.trimMargin()
        )
        val id = file.getValueDeclarationGroupByName("id")
        TestCase.assertEquals("forall a. a -> a", id.checkType().toString())
    }
}

