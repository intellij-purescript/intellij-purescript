package org.purescript.module.exports

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.purescript.getModule
import org.purescript.module.exports.ExportedData

class ExportedDataReferenceTest : BasePlatformTestCase() {

    fun `test resolves to data declaration`() {
        val module = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (Bar) where
                data Bar = Qux
            """.trimIndent()
        ).getModule()
        val exportedData =
            module.exports!!.exportedItems.single() as ExportedData.Psi
        val dataDeclaration = module.cache.dataDeclarations.single()

        TestCase.assertEquals(dataDeclaration, exportedData.reference.resolve())
    }

    fun `test resolve fails when no data declaration exists`() {
        val module = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (Bar) where
            """.trimIndent()
        ).getModule()
        val exportedData = module.exports!!.exportedItems.single() as ExportedData.Psi

        TestCase.assertNull(exportedData.reference.resolve())
    }

    fun `test completes data declarations`() {
        myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (B<caret>) where
                data Bar = Qux
                data Baz = Qux
            """.trimIndent()
        )
        myFixture.testCompletionVariants("Foo.purs", "Bar", "Baz")
    }

    fun `test finds usage of data declaration`() {
        val module = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (Bar) where
                data <caret>Bar = Qux
            """.trimIndent()
        ).getModule()
        val exportedData = module.exports!!.exportedItems.single() as ExportedData.Psi
        val usageInfo = myFixture.testFindUsages("Foo.purs").single()

        TestCase.assertEquals(exportedData, usageInfo.element)
    }

    fun `test resolves to newtype declaration`() {
        val module = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (Bar) where
                newtype Bar = Qux Int
            """.trimIndent()
        ).getModule()
        val exportedData =
            module.exports!!.exportedItems.single() as ExportedData.Psi
        val newtypeDeclaration = module.cache.newTypeDeclarations.single()

        TestCase.assertEquals(
            newtypeDeclaration,
            exportedData.reference.resolve()
        )
    }

    fun `test resolve fails when no newtype declaration exists`() {
        val module = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (Bar) where
            """.trimIndent()
        ).getModule()
        val exportedData = module.exports!!.exportedItems.single() as ExportedData.Psi

        TestCase.assertNull(exportedData.reference.resolve())
    }

    fun `test completes newtype declarations`() {
        myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (B<caret>) where
                newtype Bar = Qux Int
                newtype Baz = Qux Int
            """.trimIndent()
        )
        myFixture.testCompletionVariants("Foo.purs", "Bar", "Baz")
    }

    fun `test finds usage of newtype declaration`() {
        val module = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (Bar) where
                newtype <caret>Bar = Qux Int
            """.trimIndent()
        ).getModule()
        val exportedData = module.exports!!.exportedItems.single() as ExportedData.Psi
        val usageInfo = myFixture.testFindUsages("Foo.purs").single()

        TestCase.assertEquals(exportedData, usageInfo.element)
    }
}
