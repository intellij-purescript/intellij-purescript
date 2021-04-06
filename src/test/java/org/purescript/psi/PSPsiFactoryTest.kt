package org.purescript.psi

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class PSPsiFactoryTest : BasePlatformTestCase() {

    fun `test creates module name`() {
        with(PSPsiFactory(project)) {
            assertEquals("Foo", createModuleName("Foo")!!.name)
            assertEquals("Foo.Bar", createModuleName("Foo.Bar")!!.name)
            assertNull(createModuleName("foo"))
        }
    }
}
