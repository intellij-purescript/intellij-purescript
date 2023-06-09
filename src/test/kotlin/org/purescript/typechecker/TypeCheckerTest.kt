package org.purescript.typechecker

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.purescript.getValueDeclarationGroupByName

class TypeCheckerTest : BasePlatformTestCase() {

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

}

