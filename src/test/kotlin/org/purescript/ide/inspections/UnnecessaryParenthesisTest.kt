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

}
