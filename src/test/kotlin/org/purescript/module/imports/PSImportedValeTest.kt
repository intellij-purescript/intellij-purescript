package org.purescript.module.imports

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.purescript.getImportedValue


class PSImportedValeTest : BasePlatformTestCase() {

    fun `test imported value has correct name`() {
        val importedValue = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar (qux)
            """.trimIndent()
        ).getImportedValue()
        assertEquals("qux", importedValue.name)
    }
}
