package org.purescript.module.exports

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.purescript.getExportedData
import org.purescript.getExportedDataMember

class PSExportedDataMemberTest : BasePlatformTestCase() {
    fun `test finds name`() {
        val exportedDataMember = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (Bar(Qux)) where
            """.trimIndent()
        ).getExportedDataMember()

        assertEquals("Qux", exportedDataMember.name)
    }

    fun `test finds parent exported data`() {
        myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (Bar(Qux)) where
            """.trimIndent()
        ).run {
            assertEquals(getExportedData(), getExportedDataMember().exportedData)
        }
    }
}
