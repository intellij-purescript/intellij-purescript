package org.purescript.psi

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.purescript.file.PSFile


class PSImportedOperatorTest : BasePlatformTestCase() {

    fun `test imported operator has correct name`() {
        val file = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar ((<|~|>))
            """.trimIndent()
        ) as PSFile
        val importDecl = file.module.importDeclarations.single()
        val importedOperator = importDecl.importList!!.importedItems.single() as PSImportedOperator
        TestCase.assertEquals("<|~|>", importedOperator.name)
    }
}
