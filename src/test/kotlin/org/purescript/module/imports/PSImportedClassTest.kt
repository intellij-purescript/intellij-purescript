package org.purescript.module.imports

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.purescript.getImportedClass


class PSImportedClassTest : BasePlatformTestCase() {

    fun `test imported class has correct name`() {
        val importedClass = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar (class Qux)
            """.trimIndent()
        ).getImportedClass()
        assertEquals("Qux", importedClass.name)
    }
}
