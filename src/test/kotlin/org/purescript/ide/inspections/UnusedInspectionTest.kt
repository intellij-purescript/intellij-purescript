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

}
