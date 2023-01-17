package org.purescript.psi.expression

import com.intellij.psi.stubs.StubIndex
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.jupiter.api.Assertions.*
import org.purescript.getExpressionIdentifier
import org.purescript.getValueDeclarations

class ExpressionIdentifierCompletionContributorTest: BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        StubIndex.getInstance().forceRebuild(Throwable("Clear index in test"))
        PlatformTestUtil.dispatchAllEventsInIdeEventQueue()
    }
    
    fun `test finds values in other files`() {
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
                
                y0 = y<caret>
            """.trimIndent()
        )

        myFixture.testCompletionVariants("Foo.purs", "y0", "y1")
    }
}