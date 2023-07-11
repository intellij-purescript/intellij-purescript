package org.purescript.module.declaration.classes

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.purescript.getClassDeclaration

class PSClassFunctionalDependencyListTest : BasePlatformTestCase() {
    fun `test parses functional dependency list`() {
        val functionalDependencyList = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                class Foo a b c | a -> b, c b -> a
            """.trimIndent()
        ).getClassDeclaration().functionalDependencyList!!

        assertSize(2, functionalDependencyList.functionalDependencies)
    }
}
