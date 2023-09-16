package org.purescript.module.imports

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.purescript.getDataDeclaration
import org.purescript.getImportedData
import org.purescript.getNewTypeDeclaration

class PSImportedDataTest : BasePlatformTestCase() {

    fun `test imported data has correct name`() {
        val importedData = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar (Qux)
            """.trimIndent()
        ).getImportedData()

        assertEquals("Qux", importedData.name)
    }

    fun `test parses non-existing data member list`() {
        val importedData = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar (Qux)
            """.trimIndent()
        ).getImportedData()

        assertNull(importedData.importedDataMemberList)
        assertFalse(importedData.importsAll)
        assertEmpty(importedData.importedDataMembers)
    }

    fun `test parses double dot data member list`() {
        val importedData = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar (Qux(..))
            """.trimIndent()
        ).getImportedData()

        assertNotNull(importedData.importedDataMemberList)
        assertTrue(importedData.importsAll)
        assertEmpty(importedData.importedDataMembers)
    }

    fun `test parses empty data member list`() {
        val importedData = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar (Qux())
            """.trimIndent()
        ).getImportedData()

        assertNotNull(importedData.importedDataMemberList)
        assertFalse(importedData.importsAll)
        assertEmpty(importedData.importedDataMembers)
    }

    fun `test parses regular data member list`() {
        val importedData = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar (Qux(A, B, C))
            """.trimIndent()
        ).getImportedData()

        assertNotNull(importedData.importedDataMemberList)
        assertFalse(importedData.importsAll)
        assertSize(3, importedData.importedDataMembers)
    }

    fun `test newtype declaration property`() {
        val newTypeDeclaration = myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                newtype Qux = A Int
            """.trimIndent()
        ).getNewTypeDeclaration()
        val importedData = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar (Qux)
            """.trimIndent()
        ).getImportedData()

        assertEquals(newTypeDeclaration, importedData.newTypeDeclaration)
        assertNull(importedData.dataDeclaration)
    }

    fun `test data declaration property`() {
        val dataDeclaration = myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                data Qux = A Int
            """.trimIndent()
        ).getDataDeclaration()
        val importedData = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar (Qux)
            """.trimIndent()
        ).getImportedData()

        assertEquals(dataDeclaration, importedData.dataDeclaration)
        assertNull(importedData.newTypeDeclaration)
    }
}
