package org.purescript.psi.exports

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.purescript.getDataConstructor
import org.purescript.getExportedDataMember

class ExportedDataMemberReferenceTest : BasePlatformTestCase() {

    fun `test resolves to data constructor`() {
        myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (Bar(Qux)) where
                data Bar = Qux
            """.trimIndent()
        ).run {
            assertEquals(getDataConstructor(), getExportedDataMember().reference.resolve())
        }
    }

    fun `test doesn't resolve if wrong data constructor`() {
        myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (Bar(Qux)) where
                data Bar = Baz
            """.trimIndent()
        ).run {
            assertNull(getExportedDataMember().reference.resolve())
        }
    }

    fun `test doesn't resolve if wrong data declaration`() {
        myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (Bar(Qux)) where
                data Baz = Qux
            """.trimIndent()
        ).run {
            assertNull(getExportedDataMember().reference.resolve())
        }
    }

    fun `test completes data constructor`() {
        myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (Bar(<caret>)) where
                data Bar
                    = Basket
                    | Bat
                    | Banana
            """.trimIndent()
        )
        myFixture.testCompletionVariants("Foo.purs", "Basket", "Bat", "Banana")
    }

    fun `test finds usage of dataconstructor`() {
        val exportedDataMember = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (Bar(Banana)) where
                data Bar
                    = Basket
                    | Bat
                    | <caret>Banana
            """.trimIndent()
        ).getExportedDataMember()
        val usageInfo = myFixture.testFindUsages("Foo.purs").single()
        assertEquals(exportedDataMember, usageInfo.element)
    }
}
