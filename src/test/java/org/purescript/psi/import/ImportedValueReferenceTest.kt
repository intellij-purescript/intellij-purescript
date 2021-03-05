package org.purescript.psi.import

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.purescript.file.PSFile

class ImportedValueReferenceTest : BasePlatformTestCase() {
    fun `test resolves value declarations`() {
        val bar = myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                bar = ""
            """.trimIndent()
        ) as PSFile
        val declaration = bar.module.valueDeclarations.single()
        val foo = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar (bar)
            """.trimIndent()
        ) as PSFile
        val importedValue =
            foo.module.importDeclarations.single().importList!!.importedItems.single() as PSImportedValue
        assertEquals(declaration, importedValue.reference.resolve())
    }

    fun `test doesn't resolve non-existing value declarations`() {
        myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                qux = ""
            """.trimIndent()
        )
        val foo = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar (bar)
            """.trimIndent()
        ) as PSFile
        val importedValue =
            foo.module.importDeclarations.single().importList!!.importedItems.single() as PSImportedValue
        assertNull(importedValue.reference.resolve())
    }

    fun `test doesn't resolve hidden value declarations`() {
        myFixture.configureByText(
            "Bar.purs",
            """
                module Bar (qux) where
                bar = ""
                qux = ""
            """.trimIndent()
        )
        val foo = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar (bar)
            """.trimIndent()
        ) as PSFile
        val importedValue =
            foo.module.importDeclarations.single().importList!!.importedItems.single() as PSImportedValue
        assertNull(importedValue.reference.resolve())
    }
    fun `test resolves foreign value declarations`() {
        val bar = myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                foreign import bar :: Boolean
            """.trimIndent()
        ) as PSFile
        val declaration = bar.module.foreignValueDeclarations.single()
        val foo = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar (bar)
            """.trimIndent()
        ) as PSFile
        val importedValue =
            foo.module.importDeclarations.single().importList!!.importedItems.single() as PSImportedValue
        assertEquals(declaration, importedValue.reference.resolve())
    }

    fun `test doesn't resolve non-existing foreign value declarations`() {
        myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                foreign import qux :: Int -> Int
            """.trimIndent()
        )
        val foo = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar (bar)
            """.trimIndent()
        ) as PSFile
        val importedValue =
            foo.module.importDeclarations.single().importList!!.importedItems.single() as PSImportedValue
        assertNull(importedValue.reference.resolve())
    }

    fun `test doesn't resolve hidden foreign value declarations`() {
        myFixture.configureByText(
            "Bar.purs",
            """
                module Bar (qux) where
                foreign import bar :: Int
                foreign import qux :: Int
            """.trimIndent()
        )
        val foo = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar (bar)
            """.trimIndent()
        ) as PSFile
        val importedValue =
            foo.module.importDeclarations.single().importList!!.importedItems.single() as PSImportedValue
        assertNull(importedValue.reference.resolve())
    }

    fun `test completes value and foreign value declarations`() {
        myFixture.configureByText(
            "Bar.purs",
            """
                module Bar (fi, fo, bar) where
                fi = "tre"
                foreign import fo :: Int -> Boolean
                foreign import fum :: Int
                foreign import bar :: Int
            """.trimIndent()
        )
        myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar (f<caret>)
            """.trimIndent()
        )
        myFixture.testCompletionVariants("Foo.purs", "fi", "fo")
    }
}
