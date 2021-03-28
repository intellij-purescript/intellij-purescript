package org.purescript.psi.imports

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.purescript.getImportedItem


class PSImportedKindTest : BasePlatformTestCase() {

    fun `test imported kind has correct name`() {
        val importedKind = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar (kind Qux)
            """.trimIndent()
        ).getImportedItem() as PSImportedKind
        assertEquals("Qux", importedKind.name)
    }
}
