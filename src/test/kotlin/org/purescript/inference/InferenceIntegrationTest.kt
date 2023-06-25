package org.purescript.inference

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.purescript.getModule
import org.purescript.getValueDeclarationGroupByName

class InferenceIntegrationTest: BasePlatformTestCase() {
    fun `test everything`() {
        val Main = myFixture.configureByText(
            "Main.purs",
            """
                | module Main where
                | 
                | f a = a
                | 
                | x = f 1
                | 
                | int :: Int -> Int
                | int x = x
                | 
            """.trimMargin()
        )
        val f = Main.getValueDeclarationGroupByName("f").inferType()
        TestCase.assertEquals(InferType.function(InferType.Id(0), InferType.Id(0)).toString(), "$f")
        val int = Main.getValueDeclarationGroupByName("int").inferType()
        TestCase.assertEquals(InferType.function(InferType.Int, InferType.Int), int)
        val x = Main.getValueDeclarationGroupByName("x").inferType()
        TestCase.assertEquals(InferType.Int, x)
    }
    fun `test primitives`() {
        val Main = myFixture.configureByText(
            "Main.purs",
            """
                | module Main where
                | 
                | int = 42
                | number = 42.0
                | string = "Hello World"
                | boolean = true
            """.trimMargin()
        )
        Main.getModule().unify()
        val int = Main.getValueDeclarationGroupByName("int").infer(Scope.new())
        val number = Main.getValueDeclarationGroupByName("number").infer(Scope.new())
        val string = Main.getValueDeclarationGroupByName("string").infer(Scope.new())
        val boolean = Main.getValueDeclarationGroupByName("boolean").infer(Scope.new())
        TestCase.assertEquals("Int", int.toString())
        TestCase.assertEquals("Number", number.toString())
        TestCase.assertEquals("String", string.toString())
        TestCase.assertEquals("Boolean", boolean.toString())
    }
    fun `test records`() {
        val Main = myFixture.configureByText(
            "Main.purs",
            """
                | module Main where
                | 
                | record =
                |  { int: 42
                |  , number: 42.0
                |  , string: "Hello World"
                |  , boolean: true
                |  }
                | int = record.int
                | 
                | type User = { age:: Int, name :: String }
                | 
                | mkUser :: User -> Int
                | mkUser user = 42
                | 
                | checkUser u = mkUser u
                | 
            """.trimMargin()
        )
        val record = Main.getValueDeclarationGroupByName("record").inferType()
        TestCase.assertEquals(
            "{ int::Int, number::Number, string::String, boolean::Boolean }",
            record.toString()
        )
        val int = Main.getValueDeclarationGroupByName("int").inferType()
        TestCase.assertEquals("Int", int.toString())

        val checkUserType = Main.getValueDeclarationGroupByName("checkUser").inferType()
        TestCase.assertEquals("{ age::Int, name::String } -> Int", "$checkUserType")

    }
    fun `test signature`() {
        val Main = myFixture.configureByText(
            "Main.purs",
            """
                | module Main where
                | 
                | f :: forall a. a -> a
                | f x = x
                | 
                | eq :: forall a. Eq a => a -> a
                | eq x = x
                | 
                | int = eq 1
            """.trimMargin()
        )
        Main.getModule().unify()
        val f = Main.getValueDeclarationGroupByName("f").substitutedType
        TestCase.assertEquals("u0 -> u0", f.toString())

        val eq = Main.getValueDeclarationGroupByName("eq").substitutedType
        TestCase.assertEquals("Eq u7 => u7 -> u7", eq.toString())

        val int = Main.getValueDeclarationGroupByName("int").substitutedType
        TestCase.assertEquals("Int", int.toString())
    }
    
    fun `test union`() {
        val Main = myFixture.configureByText(
            "Main.purs",
            """
                | module Main where
                | 
                | f :: forall a. Union (name :: String) (age :: Int) a => Record a -> Int
                | f x = 42
                | 
            """.trimMargin()
        )
        val f = Main.getValueDeclarationGroupByName("f").inferType()
        TestCase.assertEquals("{ name::String, age::Int } -> Int", f.toString())
    }
}