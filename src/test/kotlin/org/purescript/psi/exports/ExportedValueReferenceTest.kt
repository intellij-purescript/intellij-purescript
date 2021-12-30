package org.purescript.psi.exports

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.purescript.*

class ExportedValueReferenceTest : BasePlatformTestCase() {

    fun `test resolves to declared value`() {
        myFixture.addFileToProject(
            "Main.purs",
            """module Main (foo) where
               foo = 3
            """.trimIndent()
        ).run {
            assertEquals(getValueDeclaration(), getExportedValue().reference.resolve())
        }
    }

    fun `test resolve fails when no declared value`() {
        val exportedValue = myFixture.addFileToProject(
            "Main.purs",
            """module Main (foo) where
               bar = 3
            """.trimIndent()
        ).getExportedValue()

        assertNull(exportedValue.reference.resolve())
    }

    fun `test resolves to all declared values`() {
        val file = myFixture.addFileToProject(
            "Main.purs",
            """module Main (foo) where
               foo true = 3
               foo false = 4
               bar false = 4
            """.trimIndent()
        )
        val exportedValue = file.getExportedValue()
        val valueDeclarations = file.getValueDeclarations()

        assertTrue(exportedValue.reference.isReferenceTo(valueDeclarations[0]))
        assertTrue(exportedValue.reference.isReferenceTo(valueDeclarations[1]))
        assertFalse(exportedValue.reference.isReferenceTo(valueDeclarations[2]))
    }

    fun `test resolves to foreign values`() {
        myFixture.addFileToProject(
            "Main.purs",
            """module Main (foo) where
               foreign import foo :: Int
            """.trimIndent()
        ).run {
            assertEquals(getForeignValueDeclaration(), getExportedValue().reference.resolve())
        }
    }

    fun `test completes exported values`() {
        myFixture.configureByText(
            "Main.purs",
            """
                module Main (f<caret>) where
                f1 = 1
                f2 = 2
                bar = 3
            """.trimIndent()
        )
        myFixture.testCompletionVariants("Main.purs", "f1", "f2")
    }

    fun `test finds usage of declared value`() {
        val exportedValue = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (foo) where
                <caret>foo = 3
            """.trimIndent()
        ).getExportedValue()
        val usageInfo = myFixture.testFindUsages("Foo.purs").single()

        assertEquals(exportedValue, usageInfo.element)
    }

    fun `test finds usage of foreign value`() {
        val exportedValue = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (foo) where
                foreign import <caret>foo :: Int
            """.trimIndent()
        ).getExportedValue()
        val usageInfo = myFixture.testFindUsages("Foo.purs").single()

        assertEquals(exportedValue, usageInfo.element)
    }

    fun `test does not find usage if caret is misplaced`() {
        myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (foo) where
                <caret>foreign import foo :: Int
            """.trimIndent()
        )
        assertThrows(AssertionError::class.java, "Cannot find referenced element") {
            myFixture.testFindUsages("Foo.purs")
        }
    }

    fun `test does not find usage of foreign value that's not used`() {
        myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (bar) where
                foreign import <caret>foo :: Int
            """.trimIndent()
        )
        assertTrue(myFixture.testFindUsages("Foo.purs").isEmpty())
    }

    fun `test resolves to class members`() {
        myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (bar) where
                class Bar where
                    bar :: Int
            """.trimIndent()
        ).run {
            assertEquals(getClassMember(), getExportedValue().reference.resolve())
        }
    }

    fun `test completes to class members`() {
        myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (b<caret>) where
                class Bar where
                    bar :: Int
                    bim :: Int
            """.trimIndent()
        )
        myFixture.testCompletionVariants("Foo.purs", "bar", "bim")
    }

    fun `test finds usages class members`() {
        val file = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (bar) where
                class Bar where
                    bar :: Int
            """.trimIndent()
        )
        val exportedValue = file.getExportedValue()
        val usageInfo = myFixture.findUsages(file.getClassMember()).single()

        run {
            assertEquals(exportedValue, usageInfo.element)
        }
    }
}
