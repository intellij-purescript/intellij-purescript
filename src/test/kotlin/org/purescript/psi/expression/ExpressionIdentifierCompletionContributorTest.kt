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
}