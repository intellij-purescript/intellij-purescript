package org.purescript.module.imports

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.purescript.*

class ImportedValueReferenceTest : BasePlatformTestCase() {

    fun `test resolves value declarations`() {
        val valueDeclaration = myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                bar = ""
            """.trimIndent()
        ).getValueDeclarationGroup()
        val importedValue = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar (bar)
            """.trimIndent()
        ).getImportedValue()
        assertEquals(valueDeclaration, importedValue.reference.resolve())
    }

    fun `test doesn't resolve non-existing value declarations`() {
        myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                qux = ""
            """.trimIndent()
        )
        val importedValue = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar (bar)
            """.trimIndent()
        ).getImportedValue()
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
        val importedValue = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar (bar)
            """.trimIndent()
        ).getImportedValue()
        assertNull(importedValue.reference.resolve())
    }

    fun `test resolves foreign value declarations`() {
        val foreignValueDeclaration = myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                foreign import bar :: Boolean
            """.trimIndent()
        ).getForeignValueDeclaration()
        val importedValue = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar (bar)
            """.trimIndent()
        ).getImportedValue()
        assertEquals(foreignValueDeclaration, importedValue.reference.resolve())
    }

    fun `test doesn't resolve non-existing foreign value declarations`() {
        myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                foreign import qux :: Int -> Int
            """.trimIndent()
        )
        val importedValue = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar (bar)
            """.trimIndent()
        ).getImportedValue()
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
        val importedValue = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar (bar)
            """.trimIndent()
        ).getImportedValue()
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

    fun `test doesn't include duplicate completions`() {
        myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                codePointAt :: Int -> String -> Maybe CodePoint
                codePointAt n _ | n < 0 = Nothing
                codePointAt 0 "" = Nothing
                codePointAt 0 s = Just (unsafeCodePointAt0 s)
                codePointAt n s = _codePointAt codePointAtFallback Just Nothing unsafeCodePointAt0 n s
                codeFixForNoLookupShown = ""
            """.trimIndent()
        )
        myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar (code<caret>)
            """.trimIndent()
        )
        assertEquals(
            listOf("codePointAt", "codeFixForNoLookupShown").sorted(),
            myFixture.getCompletionVariants("Foo.purs")!!.sorted()
        )
    }

    fun `test finds value declaration usage`() {
        val valueDeclaration = myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                bar = 3
            """.trimIndent()
        ).getValueDeclarationGroup()
        val importedValue = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar (bar)
            """.trimIndent()
        ).getImportedValue()
        val usage = myFixture.findUsages(valueDeclaration).single().element
        assertEquals(importedValue, usage)
    }

    fun `test finds foreign value declaration usage`() {
        val foreignValueDeclaration = myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                foreign import bar :: Int
            """.trimIndent()
        ).getForeignValueDeclaration()
        val importedValue = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Bar (bar)
            """.trimIndent()
        ).getImportedValue()
        val usage = myFixture.findUsages(foreignValueDeclaration).single().element
        assertEquals(importedValue, usage)
    }

    fun `test resolves class member declarations`() {
        val classMember = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                class Bar where
                    foo :: Int
            """.trimIndent()
        ).getClassMember()
        val importedValue = myFixture.configureByText(
            "Qux.purs",
            """
                module Qux where
                import Foo (foo)
            """.trimIndent()
        ).getImportedValue()

        assertEquals(classMember, importedValue.reference.resolve())
    }

    fun `test completes class member declarations`() {
        myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                class Bar where
                    foo :: Int
                    fum :: Int
            """.trimIndent()
        )
        myFixture.configureByText(
            "Qux.purs",
            """
                module Qux where
                import Foo (f<caret>)
            """.trimIndent()
        )

        myFixture.testCompletionVariants("Qux.purs", "foo", "fum")
    }

    fun `test finds usages of class member declarations`() {
        val classMember = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                class Bar where
                    foo :: Int
            """.trimIndent()
        ).getClassMember()
        val importedValue = myFixture.configureByText(
            "Qux.purs",
            """
                module Qux where
                import Foo (foo)
            """.trimIndent()
        ).getImportedValue()
        val usageInfo = myFixture.findUsages(classMember).single()

        assertEquals(importedValue, usageInfo.element)
    }
}
