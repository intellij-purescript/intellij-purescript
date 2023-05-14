package org.purescript.module.declaration.value.expression

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.purescript.getAllAccessors
import org.purescript.getLabeled

class AccessorReferenceTest : BasePlatformTestCase() {
    fun `test that accessor references labeled type`() {
        val file = myFixture.configureByText(
            "Foo.purs",
            """
                |module Foo where
                |type MyRecord = { label :: Int}
                |f r = r.label
            """.trimMargin()
        )
        val labeled = file.getLabeled()

        val accessor = file.getAllAccessors().single()

        assertEquals(labeled, accessor.reference.resolve())
    }

}
