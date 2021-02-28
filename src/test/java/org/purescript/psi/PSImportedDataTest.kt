package org.purescript.psi

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.purescript.file.PSFile


class PSImportedDataTest : BasePlatformTestCase() {

    fun `test imported data has correct name`() {
        val file = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar (Qux)
            """.trimIndent()
        ) as PSFile
        val importDecl = file.module.importDeclarations.single()
        val importedData = importDecl.importList!!.importedItems.single() as PSImportedData
        TestCase.assertEquals("Qux", importedData.name)
    }
}
