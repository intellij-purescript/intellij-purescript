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

    fun `test reports unresolved exported module (not imported)`() {
        myFixture.configureByText(
            "Bar.purs",
            """
            module Bar where
            """.trimIndent()
        )
        myFixture.configureByText(
            "Foo.purs",
            """
            module Foo (<error descr="Unresolved module 'Bar'">module Bar</error>) where
            """.trimIndent()
        )
        myFixture.enableInspections(PSUnresolvedReferenceInspection())
        myFixture.checkHighlighting()
    }

    fun `test reports unresolved exported module (not exists)`() {
        myFixture.configureByText(
            "Foo.purs",
            """
            module Foo (<error descr="Unresolved module 'Bar'">module Bar</error>) where
            import Bar
            """.trimIndent()
        )
        myFixture.enableInspections(PSUnresolvedReferenceInspection())
        myFixture.checkHighlighting()
    }

    fun `test doesn't report resolved exported module`() {
        myFixture.configureByText(
            "Bar.purs",
            """
            module Bar where
            """.trimIndent()
        )
        myFixture.configureByText(
            "Foo.purs",
            """
            module Foo (module Bar) where
            import Bar
            """.trimIndent()
        )
        myFixture.enableInspections(PSUnresolvedReferenceInspection())
        myFixture.checkHighlighting()
    }
}
