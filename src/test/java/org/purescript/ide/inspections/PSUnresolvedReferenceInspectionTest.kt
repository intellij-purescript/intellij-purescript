package org.purescript.ide.inspections

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class PSUnresolvedReferenceInspectionTest : BasePlatformTestCase() {

    fun `test reports unresolved exported value`() {
        myFixture.configureByText(
            "Foo.purs",
            """
            module Foo (<error descr="Cannot resolve symbol 'foo'">foo</error>) where
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
            module Foo (module <error descr="Cannot resolve symbol 'Bar'">Bar</error>) where
            """.trimIndent()
        )
        myFixture.enableInspections(PSUnresolvedReferenceInspection())
        myFixture.checkHighlighting()
    }

    fun `test reports unresolved imported and exported module (not exists)`() {
        myFixture.configureByText(
            "Foo.purs",
            """
            module Foo (module <error descr="Cannot resolve symbol 'Bar'">Bar</error>) where
            import <error descr="Cannot resolve symbol 'Bar'">Bar</error>
            """.trimIndent()
        )
        myFixture.enableInspections(PSUnresolvedReferenceInspection())
        myFixture.checkHighlighting()
    }

    fun `test doesn't report resolved imported and exported module`() {
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

    fun `test doesn't report unresolved built-in modules`() {
        myFixture.configureByText(
            "Foo.purs",
            """
            module Foo
                ( module Prim
                , module Prim.Boolean
                , module Prim.Coerce
                , module Prim.Ordering
                , module Prim.Row
                , module Prim.RowList
                , module Prim.Symbol
                , module Prim.TypeError
                ) where
            import Prim
            import Prim.Boolean
            import Prim.Coerce
            import Prim.Ordering
            import Prim.Row
            import Prim.RowList
            import Prim.Symbol
            import Prim.TypeError
            """.trimIndent()
        )
        myFixture.enableInspections(PSUnresolvedReferenceInspection())
        myFixture.checkHighlighting()
    }

    fun `test doesn't report resolved expression constructors`() {
        myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                newtype Bar = Qux Int
                f = Qux 3
            """.trimIndent()
        )

        myFixture.enableInspections(PSUnresolvedReferenceInspection())
        myFixture.checkHighlighting()
    }

    fun `test reports unresolved expression constructors`() {
        myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                f = <error descr="Cannot resolve symbol 'Bar'">Bar</error> 3
            """.trimIndent()
        )

        myFixture.enableInspections(PSUnresolvedReferenceInspection())
        myFixture.checkHighlighting()
    }

    fun `test reports unresolved expression vars`() {
        myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                f = <error descr="Cannot resolve symbol 'sort'">sort</error>
            """.trimIndent()
        )

        myFixture.enableInspections(PSUnresolvedReferenceInspection())
        myFixture.checkHighlighting()
    }

    fun `test finds function with two definitions`() {
        myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                
                x 1 = 0
                x _ = 2
                
                f = x 1
            """.trimIndent()
        )

        myFixture.enableInspections(PSUnresolvedReferenceInspection())
        myFixture.checkHighlighting()
    }

    fun `test errors when constructor is missing`() {
        myFixture.configureByText(
            "Foo.purs",
            """module Foo where

            f (<error descr="Cannot resolve symbol 'Just'">Just</error> n) = n
            f _ = 0
            """.trimIndent()
        )

        myFixture.enableInspections(PSUnresolvedReferenceInspection())
        myFixture.checkHighlighting()
    }

    fun `test it finds local constructors for binders`() {
        myFixture.configureByText(
            "Foo.purs",
            """module Foo where
            data Box a = Box a
            f Box a = a
            """.trimIndent()
        )

        myFixture.enableInspections(PSUnresolvedReferenceInspection())
        myFixture.checkHighlighting()
    }

    fun `test it finds local class members`() {
        myFixture.configureByText(
            "Foo.purs",
            """
            module Foo where
            class Box a where
                map :: forall a b. (a -> b) -> Box a -> Box b 
            f = map
            """.trimIndent()
        )

        myFixture.enableInspections(PSUnresolvedReferenceInspection())
        myFixture.checkHighlighting()
    }

    fun `test it finds imported class members`() {
        myFixture.configureByText(
            "Box.purs",
            """
            module Box where
            
            class Box a where
                map :: forall a b. (a -> b) -> Box a -> Box b 
            """.trimIndent()
        )

        myFixture.configureByText(
            "Foo.purs",
            """
            module Foo where
            
            import Box
            
            f = map
            """.trimIndent()
        )

        myFixture.enableInspections(PSUnresolvedReferenceInspection())
        myFixture.checkHighlighting()
    }

    fun `test it finds imported class members by class name`() {
        myFixture.configureByText(
            "Box.purs",
            """
            module Box where
            
            class Box a where
                map :: forall a b. (a -> b) -> Box a -> Box b 
            """.trimIndent()
        )
        myFixture.configureByText(
            "Foo.purs",
            """
            module Foo where
            
            import Box (class Box)
            
            f = map
            """.trimIndent()
        )
        myFixture.enableInspections(PSUnresolvedReferenceInspection())
        myFixture.checkHighlighting()
    }

    fun `test it finds imported class members by name`() {
        myFixture.configureByText(
            "Box.purs",
            """
            module Box where
            
            class Box a where
                map :: forall a b. (a -> b) -> Box a -> Box b 
            """.trimIndent()
        )
        myFixture.configureByText(
            "Foo.purs",
            """
            module Foo where
            
            import Box (map)
            
            f = map
            """.trimIndent()
        )
        myFixture.enableInspections(PSUnresolvedReferenceInspection())
        myFixture.checkHighlighting()
    }


    fun `test it finds value declaration in where`() {

        myFixture.configureByText(
            "Foo.purs",
            """
            module Foo where
            
            f = x
              where x = 1
            """.trimIndent()
        )

        myFixture.enableInspections(PSUnresolvedReferenceInspection())
        myFixture.checkHighlighting()
    }

    fun `test it finds value declaration in let`() {

        myFixture.configureByText(
            "Foo.purs",
            """
            module Foo where
            
            f = let x = 1 in x
            """.trimIndent()
        )

        myFixture.enableInspections(PSUnresolvedReferenceInspection())
        myFixture.checkHighlighting()
    }
}
