package org.purescript.psi.imports

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.purescript.getImportedData


class PSImportedDataTest : BasePlatformTestCase() {

    fun `test imported data has correct name`() {
        val importedData = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar (Qux)
            """.trimIndent()
        ).getImportedData()
        assertEquals("Qux", importedData.name)
    }
}
