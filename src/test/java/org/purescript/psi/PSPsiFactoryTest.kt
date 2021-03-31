package org.purescript.psi

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class PSPsiFactoryTest : BasePlatformTestCase() {

    fun `test create qualified proper name`() {
        with(PSPsiFactory(project)) {
            assertEquals("Foo", createQualifiedProperName("Foo")!!.name)
            assertEquals("Foo.Bar", createQualifiedProperName("Foo.Bar")!!.name)
            assertNull(createQualifiedProperName("foo"))
        }
    }
}
