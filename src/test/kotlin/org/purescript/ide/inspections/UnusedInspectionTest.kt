package org.purescript.ide.inspections

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class UnusedInspectionTest : BasePlatformTestCase() {

    fun `test it reports functions that are not exported`() {
        myFixture.configureByText(
            "Foo.purs",
            """
            |module Foo where
            |foo :: Int
            |<warning descr="Unused value declaration">foo</warning> = 1
            """.trimMargin()
        )
        myFixture.enableInspections(UnusedInspection())
        myFixture.checkHighlighting()
    }
    
    fun `test it dont report functions that are exported`() {
        myFixture.configureByText(
            "Foo.purs",
            """
            |module Foo (foo) where
            |foo :: Int
            |foo = 1
            """.trimMargin()
        )
        myFixture.enableInspections(UnusedInspection())
        myFixture.checkHighlighting()
    }  
    
    fun `test it dont report instance members`() {
        myFixture.configureByText(
            "Foo.purs",
            """
            |module Foo (Box(Box)) where
            |
            |data Box a = Box a
            |
            |instance Show a => Show Box a where
            |   show (Box a) = "(Box " <> show a <> ")"
            """.trimMargin()
        )
        myFixture.enableInspections(UnusedInspection())
        myFixture.checkHighlighting()
    } 
    
    fun `test it report unused value imports`() {
        myFixture.configureByText(
            "Bar.purs",
            """
            |module Bar (bar) where
            |
            |bar :: Int
            |bar = 1
            """.trimMargin()
        )
        myFixture.configureByText(
            "Foo.purs",
            """
            |module Foo where
            |
            |import Bar (<warning>bar</warning>)
            """.trimMargin()
        )
        myFixture.enableInspections(UnusedInspection())
        myFixture.checkHighlighting()
    }
    
    fun `test it don't report unused value imports if the alias is exported`() {
        myFixture.configureByText(
            "Bar.purs",
            """
            |module Bar (bar) where
            |
            |bar :: Int
            |bar = 1
            """.trimMargin()
        )
        myFixture.configureByText(
            "Foo.purs",
            """
            |module Foo (module Bar) where
            |
            |import Bar (bar) as Bar
            """.trimMargin()
        )
        myFixture.enableInspections(UnusedInspection())
        myFixture.checkHighlighting()
    }

}
