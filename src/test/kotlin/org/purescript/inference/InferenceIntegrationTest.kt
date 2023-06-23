package org.purescript.inference

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.purescript.getValueDeclarationGroupByName

class InferenceIntegrationTest: BasePlatformTestCase() {
    fun `test everything`() {
        val xScope = Scope.new()
        val fScope = Scope.new()
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
        val f = Main.getValueDeclarationGroupByName("f")
        val x = Main.getValueDeclarationGroupByName("x")
        val int = Main.getValueDeclarationGroupByName("int")
        TestCase.assertEquals(
            Type.function(fScope.lookup("a"), fScope.lookup("a")),
            f.infer(fScope)
        )
        TestCase.assertEquals(
            Type.function(Type.Int, Type.Int),
            int.infer(Scope.new())
        )
        val xValue = x.valueDeclarations.single().value!!
        TestCase.assertEquals(Type.Int, xValue.infer(xScope))
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
                | boolean = True
            """.trimMargin()
        )
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
                |  , boolean: True
                |  }
                | int = record.int
            """.trimMargin()
        )
        val record = Main.getValueDeclarationGroupByName("record").infer(Scope.new())
        TestCase.assertEquals(
            "{ int::Int, number::Number, string::String, boolean::Boolean }",
            record.toString()
        )
        val int = Main.getValueDeclarationGroupByName("int").infer(Scope.new())
        TestCase.assertEquals("Int", int.toString())
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
        val f = Main.getValueDeclarationGroupByName("f").infer(Scope.new())
        TestCase.assertEquals("u1 -> u1", f.toString())
        
        val eq = Main.getValueDeclarationGroupByName("eq").infer(Scope.new())
        TestCase.assertEquals("Eq u1 => u1 -> u1", eq.toString())
        
        val int = Main.getValueDeclarationGroupByName("int").infer(Scope.new())
        TestCase.assertEquals("Int", int.toString())
    }
}