package org.purescript.psi.import

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.purescript.file.PSFile

class ImportedDataReferenceTest : BasePlatformTestCase() {

    fun `test resolves newtype declarations`() {
        val bar = myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                newtype Bar = Bar Int
            """.trimIndent()
        ) as PSFile
        val declaration = bar.module.newTypeDeclarations.single()
        val foo = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar (Bar)
            """.trimIndent()
        ) as PSFile
        val importedValue =
            foo.module.importDeclarations.single().importList!!.importedItems.single() as PSImportedData
        assertEquals(declaration, importedValue.reference.resolve())
    }

    fun `test doesn't resolve non-existing newtype declarations`() {
        myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
            """.trimIndent()
        )
        val foo = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar (Bar)
            """.trimIndent()
        ) as PSFile
        val importedValue =
            foo.module.importDeclarations.single().importList!!.importedItems.single() as PSImportedData
        assertNull(importedValue.reference.resolve())
    }

    fun `test doesn't resolve hidden newtype declarations`() {
        myFixture.configureByText(
            "Bar.purs",
            """
                module Bar (Qux) where
                newtype Bar = Bar String
                newtype Qux = Q Int
            """.trimIndent()
        )
        val foo = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar (Bar)
            """.trimIndent()
        ) as PSFile
        val importedValue =
            foo.module.importDeclarations.single().importList!!.importedItems.single() as PSImportedData
        assertNull(importedValue.reference.resolve())
    }

    fun `test completes newtype declarations`() {
        myFixture.configureByText(
            "Bar.purs",
            """
                module Bar (Bara, Bira) where
                newtype Bara = Bara (Int -> Boolean)
                newtype Bira = Bira Int
                newtype Bar = Bar Int
            """.trimIndent()
        )
        myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar (B<caret>)
            """.trimIndent()
        )
        myFixture.testCompletionVariants("Foo.purs", "Bara", "Bira")
    }

    fun `test finds newtype declaration usage`() {
        val bar = myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                newtype Bar = Bar String
            """.trimIndent()
        ) as PSFile
        val declaration = bar.module.newTypeDeclarations.single()
        val foo = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar (Bar)
            """.trimIndent()
        ) as PSFile
        val importedValue = foo.module.importDeclarations.single().importList!!.importedItems.single()
        val usage = myFixture.findUsages(declaration).single().element
        assertEquals(importedValue, usage)
    }
}
