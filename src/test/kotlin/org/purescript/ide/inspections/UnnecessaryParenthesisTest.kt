package org.purescript.ide.inspections

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class UnnecessaryParenthesisTest : BasePlatformTestCase() {

    fun `test it reports single parenthesis`() {
        myFixture.configureByText(
            "Foo.purs",
            """
            |module Foo where
            |foo = <weak_warning descr="Unnecessary parentheses">(1)</weak_warning>
            """.trimMargin()
        )
        myFixture.enableInspections(UnnecessaryParenthesis())
        myFixture.checkHighlighting()
    } 
    
    fun `test it reports single parenthesis with expression`() {
        myFixture.configureByText(
            "Foo.purs",
            """
            |module Foo where
            |foo = <weak_warning descr="Unnecessary parentheses">(1 + 1)</weak_warning>
            """.trimMargin()
        )
        myFixture.enableInspections(UnnecessaryParenthesis())
        myFixture.checkHighlighting()
    } 
    
    fun `test it reports single parenthesis around caller`() {
        myFixture.configureByText(
            "Foo.purs",
            """
            |module Foo where
            |f n = n
            |foo = <weak_warning descr="Unnecessary parentheses">(f)</weak_warning> 1
            """.trimMargin()
        )
        myFixture.enableInspections(UnnecessaryParenthesis())
        myFixture.checkHighlighting()
    }    
    
    fun `test it doesn't reports single parenthesis around argument that is a call`() {
        myFixture.configureByText(
            "Foo.purs",
            """
            |module Foo where
            |f n = n
            |foo = f (f 1)
            """.trimMargin()
        )
        myFixture.enableInspections(UnnecessaryParenthesis())
        myFixture.checkHighlighting()
    }
    
    fun `test it doesn't reports single parenthesis around caller that is not a call`() {
        myFixture.configureByText(
            "Foo.purs",
            """
            |module Foo where
            |f n = n
            |foo = (f $ f) 1
            """.trimMargin()
        )
        myFixture.enableInspections(UnnecessaryParenthesis())
        myFixture.checkHighlighting()
    }
    
    fun `test it reports parenthesis around nested call`() {
        myFixture.configureByText(
            "Foo.purs",
            """
            |module Foo where
            |f n = n
            |foo = <weak_warning descr="Unnecessary parentheses">(f f)</weak_warning> 1
            """.trimMargin()
        )
        myFixture.enableInspections(UnnecessaryParenthesis())
        myFixture.checkHighlighting()
    }    
    
    fun `test it reports parenthesis around binder in case`() {
        myFixture.configureByText(
            "Foo.purs",
            """
            |module Foo where
            |foo = case _ of
            |   <weak_warning descr="Unnecessary parentheses">(a)</weak_warning> -> a
            """.trimMargin()
        )
        myFixture.enableInspections(UnnecessaryParenthesis())
        myFixture.checkHighlighting()
    }    
    
    fun `test it don't report parenthesis around binder in parameter`() {
        myFixture.configureByText(
            "Foo.purs",
            """
            |module Foo where
            |
            |newtype Box = Box Int
            |
            |foo (Box a) = a
            """.trimMargin()
        )
        myFixture.enableInspections(UnnecessaryParenthesis())
        myFixture.checkHighlighting()
    }
    
    fun `test it dont reports parenthesis around multiple binder in case`() {
        myFixture.configureByText(
            "Foo.purs",
            """
            |module Foo where
            |
            |data Pair a b = Pair a b 
            |
            |foo = case _ of
            |   Pair (Pair a b) c -> a
            """.trimMargin()
        )
        myFixture.enableInspections(UnnecessaryParenthesis())
        myFixture.checkHighlighting()
    }   
    
    fun `test it reports parenthesis around multiple binder in record label binder`() {
        myFixture.configureByText(
            "Foo.purs",
            """
            |module Foo where
            |
            |data Pair a b = Pair a b 
            |
            |foo = case _ of
            |  { x: <weak_warning descr="Unnecessary parentheses">(Pair a b)</weak_warning> } -> a
            """.trimMargin()
        )
        myFixture.enableInspections(UnnecessaryParenthesis())
        myFixture.checkHighlighting()
    }
    fun `test it don't report parenthesis around on of multiple binder in record label binder`() {
        myFixture.configureByText(
            "Foo.purs",
            """
            |module Foo where
            |
            |data Pair a b = Pair a b 
            |
            |foo = case _ of
            |  { x: Pair (Pair a b) c } -> a
            """.trimMargin()
        )
        myFixture.enableInspections(UnnecessaryParenthesis())
        myFixture.checkHighlighting()
    }
    fun `test it don't report parenthesis around on of multiple binder in record label binder #2`() {
        myFixture.configureByText(
            "Foo.purs",
            """
            |module Foo where
            |
            |data Box a = Box a 
            |
            |foo = case _ of
            |  { x: Box (Box a) } -> a
            """.trimMargin()
        )
        myFixture.enableInspections(UnnecessaryParenthesis())
        myFixture.checkHighlighting()
    }
    
    fun `test it dont report parenthesis in the middle of a operator expression`() {
        myFixture.configureByText(
            "Foo.purs",
            """
            |module Foo where
            |l = x : (xs <#> f) <#> f'
            """.trimMargin()
        )
        myFixture.enableInspections(UnnecessaryParenthesis())
        myFixture.checkHighlighting()
    }
    
    fun `test it dont report parenthesis with accessor`() {
        myFixture.configureByText(
            "Foo.purs",
            """
            |module Foo where
            |f r = r
            |x = (f {x: 1}).x
            """.trimMargin()
        )
        myFixture.enableInspections(UnnecessaryParenthesis())
        myFixture.checkHighlighting()
    }
    
    
    fun `test it dont report parenthesis with if followed by operator`() {
        myFixture.configureByText(
            "Foo.purs",
            """
            |module Foo where
            |x = (if _ then 1 else 2) + 1
            """.trimMargin()
        )
        myFixture.enableInspections(UnnecessaryParenthesis())
        myFixture.checkHighlighting()
    }    
    fun `test it report parenthesis with if not followed by operator`() {
        myFixture.configureByText(
            "Foo.purs",
            """
            |module Foo where
            |x = <weak_warning descr="Unnecessary parentheses">(if _ then 1 else 2)</weak_warning>
            """.trimMargin()
        )
        myFixture.enableInspections(UnnecessaryParenthesis())
        myFixture.checkHighlighting()
    }
    fun `test it dont report parenthesis around operator section`() {
        myFixture.configureByText(
            "Foo.purs",
            """
            |module Foo where
            |x = (_ + 1)
            |y = (1 + _)
            """.trimMargin()
        )
        myFixture.enableInspections(UnnecessaryParenthesis())
        myFixture.checkHighlighting()
    }
    fun `test it dont report parenthesis around typed calls with arguments`() {
        myFixture.configureByText(
            "Foo.purs",
            """
            |module Foo where
            |f n = n
            |x = (f 1) :: Int
            """.trimMargin()
        )
        myFixture.enableInspections(UnnecessaryParenthesis())
        myFixture.checkHighlighting()
    }
    fun `test it report parenthesis around accessor and its identifier when being a argument`() {
        myFixture.configureByText(
            "Foo.purs",
            """
            |module Foo where
            |foo x = f <weak_warning descr="Unnecessary parentheses">(x.y)</weak_warning>
            """.trimMargin()
        )
        myFixture.enableInspections(UnnecessaryParenthesis())
        myFixture.checkHighlighting()
    }
    
    fun `test dont it report parenthesis around accessor and its identifier when it is part of a expression`() {
        myFixture.configureByText(
            "Foo.purs",
            """
            |module Foo where
            |foo x = f (x.y + 1)
            """.trimMargin()
        )
        myFixture.enableInspections(UnnecessaryParenthesis())
        myFixture.checkHighlighting()
    }    
    fun `test dont it report parenthesis around expression before record update`() {
        myFixture.configureByText(
            "Foo.purs",
            """
            |module Foo where
            |foo = (f {}) {x = 1}
            """.trimMargin()
        )
        myFixture.enableInspections(UnnecessaryParenthesis())
        myFixture.checkHighlighting()
    }    
    fun `test dont it report parenthesis around lambdas in expression`() {
        myFixture.configureByText(
            "Foo.purs",
            """
            |module Foo where
            |foo = (\ x -> x) $ 1
            """.trimMargin()
        )
        myFixture.enableInspections(UnnecessaryParenthesis())
        myFixture.checkHighlighting()
    }
}
