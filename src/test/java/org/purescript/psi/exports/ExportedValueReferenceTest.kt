package org.purescript.psi.exports

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.purescript.file.PSFile
import org.purescript.getClassMember
import org.purescript.getExportedValue

class ExportedValueReferenceTest : BasePlatformTestCase() {

    fun `test resolves to declared value`() {
        val file = myFixture.addFileToProject(
            "Main.purs",
            """module Main (foo) where
               foo = 3
            """.trimIndent()
        ) as PSFile
        val exportedValue = file.module.exportList!!.exportedItems.single() as PSExportedValue
        val declaredValue = file.module.valueDeclarations.single()
        val resolvedReference = exportedValue.reference.resolve()

        assertEquals(declaredValue, resolvedReference)
    }

    fun `test resolve fails when no declared value`() {
        val file = myFixture.addFileToProject(
            "Main.purs",
            """module Main (foo) where
               bar = 3
            """.trimIndent()
        ) as PSFile
        val exportedValue = file.module.exportList!!.exportedItems.single() as PSExportedValue
        val resolvedReference = exportedValue.reference.resolve()

        assertNull(resolvedReference)
    }

    fun `test resolves to all declared values`() {
        val file = myFixture.addFileToProject(
            "Main.purs",
            """module Main (foo) where
               foo true = 3
               foo false = 4
               bar false = 4
            """.trimIndent()
        ) as PSFile
        val exportedValue = file.module.exportList!!.exportedItems.single() as PSExportedValue
        val valueDeclarations = file.module.valueDeclarations.toList()
        val firstFooDeclaration = valueDeclarations[0]
        val secondFooDeclaration = valueDeclarations[1]
        val barDeclaration = valueDeclarations[2]

        assertTrue(exportedValue.reference.isReferenceTo(firstFooDeclaration))
        assertTrue(exportedValue.reference.isReferenceTo(secondFooDeclaration))
        assertFalse(exportedValue.reference.isReferenceTo(barDeclaration))
    }

    fun `test resolves to foreign values`() {
        val file = myFixture.addFileToProject(
            "Main.purs",
            """module Main (foo) where
               foreign import foo :: Int
            """.trimIndent()
        ) as PSFile
        val module = file.module
        val exportedValue = module.exportList!!.exportedItems.single() as PSExportedValue
        val foreignValueDeclaration = module.foreignValueDeclarations.single()

        assertTrue(exportedValue.reference.isReferenceTo(foreignValueDeclaration))
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
        ) as PSFile
        myFixture.testCompletionVariants("Main.purs", "f1", "f2")
    }

    fun `test finds usage of declared value`() {
        val file = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (foo) where
                <caret>foo = 3
            """.trimIndent()
        ) as PSFile
        val exportedValue = file.module.exportList!!.exportedItems.single()
        assertEquals(exportedValue, myFixture.testFindUsages("Foo.purs").single().element)
    }

    fun `test finds usage of foreign value`() {
        val file = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (foo) where
                foreign import <caret>foo :: Int
            """.trimIndent()
        ) as PSFile
        val exportedValue = file.module.exportList!!.exportedItems.single()
        assertEquals(exportedValue, myFixture.testFindUsages("Foo.purs").single().element)
    }

    fun `test does not find usage if caret is misplaced`() {
        myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (foo) where
                <caret>foreign import foo :: Int
            """.trimIndent()
        )
        assertThrows<AssertionError>(AssertionError::class.java, "Cannot find referenced element") {
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
        myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (bar) where
                class Bar where
                    bar :: Int
            """.trimIndent()
        ).run {
            val usageInfo = myFixture.findUsages(getClassMember()).single()
            assertEquals(getExportedValue(), usageInfo.element)
        }
    }
}
