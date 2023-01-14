package org.purescript.psi.data

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.purescript.getDataDeclaration

class DataConstructorListTest : BasePlatformTestCase() {

    fun `test parses data constructor list`() {
        val dataDeclaration = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                data Foo
                    = Bar
                    | Qux
                    | Baz
            """.trimIndent()
        ).getDataDeclaration()
        val dataConstructors = dataDeclaration.dataConstructorList!!.dataConstructors

        assertSize(3, dataConstructors)
    }

    fun `test parses complex constructor list`() {
        val dataDeclaration = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                data Foo a b
                    = Bar a
                    | Qux Int a
                    | Baz (a -> b) (List a)
            """.trimIndent()
        ).getDataDeclaration()
        val dataConstructors = dataDeclaration.dataConstructorList!!.dataConstructors

        assertSize(3, dataConstructors)
    }
}
