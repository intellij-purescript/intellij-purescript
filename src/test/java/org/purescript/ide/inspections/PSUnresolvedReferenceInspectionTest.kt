package org.purescript.ide.inspections

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class PSUnresolvedReferenceInspectionTest : BasePlatformTestCase() {

    fun `test reports unresolved exported value`() {
        myFixture.configureByText(
            "Foo.purs",
            """
            module Foo (<error descr="Unresolved reference 'foo'">foo</error>) where
            """.trimIndent()
        )
        myFixture.enableInspections(PSUnresolvedReferenceInspection())
        myFixture.checkHighlighting()
    }

    fun `test doesn't report resolved exported value`() {
        myFixture.configureByText(
            "Foo.purs",
            """
            module Foo (foo) where
            foo = 1
            """.trimIndent()
        )
        myFixture.enableInspections(PSUnresolvedReferenceInspection())
        myFixture.checkHighlighting()
    }

    fun `test doesn't report exported value resolving to foreign value`() {
        myFixture.configureByText(
            "Foo.purs",
            """
            module Foo (foo) where
            foreign import foo :: Int
            """.trimIndent()
        )
        myFixture.enableInspections(PSUnresolvedReferenceInspection())
        myFixture.checkHighlighting()
    }
}
