package org.purescript.features

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.purescript.file.PSFile

class LightFindUsageTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    fun `test find usage top level`() {
        val main = myFixture.configureByText("Main.purs",
            """
                module Main (fn) where
                fn x = x + 1
                y = fn 2
                """.trimIndent()
        ) as PSFile.Psi
        val fn = main.topLevelValueDeclarations["fn"]!!.first()
        val usages = myFixture.findUsages(fn)
        assertNotEmpty(usages)
    }

    fun `test find usage simple parameter`() {
        val main = myFixture.configureByText("Main.purs",
            """
                module Main (fn) where
                fn x = x + 1
                y = fn 2
                """.trimIndent()
        ) as PSFile.Psi
        val fn = main.topLevelValueDeclarations["fn"]!!.first()
        val x = fn.varBindersInParameters["x"]!!
        val usages = myFixture.findUsages(x)
        assertNotEmpty(usages)
    }

    fun `test find of operator re exported with module name`() {
        myFixture.configureByText(
            "Main.purs",
            """
            module Main where
            import Lib
            f = 1 + 2
            """.trimIndent()
        )
        myFixture.configureByText(
            "Lib.purs",
            """
            module Lib (module Add) where
            import Add
            """.trimIndent()
        )
        myFixture.configureByText(
            "Add.purs",
            """
            module Add where
            add a b = b
            infix 5 add as <caret>+
            """.trimIndent()
        )
        val usages = myFixture.findUsages(myFixture.elementAtCaret)
        assertSize(1, usages)
    }
}