package org.purescript.module.imports

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.purescript.getImportedItem
import org.purescript.module.declaration.imports.PSImportedOperator


class PSImportedOperatorTest : BasePlatformTestCase() {

    fun `test imported operator has correct name`() {
        val importedOperator = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar ((<|~|>))
            """.trimIndent()
        ).getImportedItem() as PSImportedOperator
        assertEquals("<|~|>", importedOperator.name)
    }
}
