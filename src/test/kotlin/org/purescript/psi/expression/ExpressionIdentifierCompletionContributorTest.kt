package org.purescript.psi.expression

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl

class ExpressionIdentifierCompletionContributorTest: BasePlatformTestCase() {
    
    fun `test dont find values in other files withg only one complete`() {
        myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                
                y1 = 1
            """.trimIndent()
        )
        myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                
                y2 = 1
                y0 = y<caret>
            """.trimIndent()
        )
        myFixture.testCompletionVariants("Foo.purs", "y0", "y2")
    }
    fun `test finds values in other files with extended complete`() {
        myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                
                y1 = 1
            """.trimIndent()
        )
        myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                y2 = 1
                y0 = y<caret>
            """.trimIndent()
        )

        myFixture.testCompletionVariants("Foo.purs", "y0", "y2")
        myFixture.configureByFiles("Foo.purs")
        myFixture.complete(CompletionType.BASIC, 2)
        val result = myFixture.lookupElementStrings!!
        assertSameElements(result, "y0", "y2", "y1")
    }
    fun `test imports the file if there is only one`() {
        myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                
                x1 = 1
            """.trimIndent()
        )
        myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                
                y0 = x<caret>
            """.trimIndent()
        )

        myFixture.complete(CompletionType.BASIC, 2)
        myFixture.checkResult(
            "Foo.purs",
            """
                |module Foo where
                |
                |import Bar (x1)
                |
                |y0 = x1
            """.trimMargin(), true)
    }
    fun `test imports with namespace if qualified`() {
        myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                
                x1 = 1
            """.trimIndent()
        )
        myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                
                y0 = Bar.x<caret>
            """.trimIndent()
        )

        myFixture.complete(CompletionType.BASIC, 2)
        myFixture.checkResult(
            "Foo.purs",
            """
                |module Foo where
                |
                |import Bar (x1) as Bar
                |
                |y0 = Bar.x1
            """.trimMargin(), true)
    }
}