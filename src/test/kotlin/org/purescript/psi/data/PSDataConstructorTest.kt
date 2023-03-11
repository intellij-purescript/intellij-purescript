package org.purescript.psi.data

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.purescript.getDataConstructor

class PSDataConstructorTest : BasePlatformTestCase() {

    fun `test parses simple constructor`() {
        val dataConstructor = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                data Bar = Qux
            """.trimIndent()
        ).getDataConstructor()

        assertEquals("Qux", dataConstructor.name)
    }
}
