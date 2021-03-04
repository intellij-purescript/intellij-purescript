package org.purescript.psi

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.purescript.file.PSFile
import org.purescript.psi.import.PSImportedValue


class PSImportedValeTest : BasePlatformTestCase() {

    fun `test imported value has correct name`() {
        val file = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar (qux)
            """.trimIndent()
        ) as PSFile
        val importDecl = file.module.importDeclarations.single()
        val importedValue = importDecl.importList!!.importedItems.single() as PSImportedValue
        TestCase.assertEquals("qux", importedValue.name)
    }
}
