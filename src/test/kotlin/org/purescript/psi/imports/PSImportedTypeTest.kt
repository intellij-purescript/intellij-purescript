package org.purescript.psi.imports

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.purescript.getImportedItem
import org.purescript.module.declaration.imports.PSImportedType


class PSImportedTypeTest : BasePlatformTestCase() {

    fun `test imported type has correct name`() {
        val importedType = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar (type (<~>))
            """.trimIndent()
        ).getImportedItem() as PSImportedType
        assertEquals("<~>", importedType.name)
    }
}
