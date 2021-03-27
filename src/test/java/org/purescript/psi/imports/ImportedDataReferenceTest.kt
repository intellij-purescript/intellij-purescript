package org.purescript.psi.imports

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.purescript.getDataDeclaration
import org.purescript.getImportedData
import org.purescript.getImportedItem
import org.purescript.getNewTypeDeclaration

class ImportedDataReferenceTest : BasePlatformTestCase() {

    fun `test resolves newtype declarations`() {
        val newTypeDeclaration = myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                newtype Bar = Bar Int
            """.trimIndent()
        ).getNewTypeDeclaration()
        val importedData = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar (Bar)
            """.trimIndent()
        ).getImportedData()
        assertEquals(newTypeDeclaration, importedData.reference.resolve())
    }

    fun `test doesn't resolve non-existing newtype declarations`() {
        myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
            """.trimIndent()
        )
        val importedData = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar (Bar)
            """.trimIndent()
        ).getImportedData()
        assertNull(importedData.reference.resolve())
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
        val importedData = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar (Bar)
            """.trimIndent()
        ).getImportedData()
        assertNull(importedData.reference.resolve())
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
        val newTypeDeclaration = myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                newtype Bar = Bar String
            """.trimIndent()
        ).getNewTypeDeclaration()
        val importedData = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar (Bar)
            """.trimIndent()
        ).getImportedItem()
        val usage = myFixture.findUsages(newTypeDeclaration).single().element
        assertEquals(importedData, usage)
    }

    fun `test resolves data declarations`() {
        val dataDeclaration = myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                data Bar = Bar Int
            """.trimIndent()
        ).getDataDeclaration()
        val importedData = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar (Bar)
            """.trimIndent()
        ).getImportedData()
        assertEquals(dataDeclaration, importedData.reference.resolve())
    }

    fun `test completes data declarations`() {
        myFixture.configureByText(
            "Bar.purs",
            """
                module Bar (Bara, Bira) where
                data Bara = Bara (Int -> Boolean)
                data Bira = Bira Int | Borat String
                data Bar = Bar Int
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

    fun `test finds data declaration usage`() {
        val dataDeclaration = myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                data Bar = Bar String
            """.trimIndent()
        ).getDataDeclaration()
        val importedData = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar (Bar)
            """.trimIndent()
        ).getImportedItem()
        val usage = myFixture.findUsages(dataDeclaration).single().element
        assertEquals(importedData, usage)
    }
}
