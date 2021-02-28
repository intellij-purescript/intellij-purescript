package org.purescript.psi

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.purescript.file.PSFile


class PSImportedClassTest : BasePlatformTestCase() {

    fun `test imported class has correct name`() {
        val file = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar (class Qux)
            """.trimIndent()
        ) as PSFile
        val importDecl = file.module.importDeclarations.single()
        val importedClass = importDecl.importList!!.importedItems.single() as PSImportedClass
        TestCase.assertEquals("Qux", importedClass.name)
    }
}
