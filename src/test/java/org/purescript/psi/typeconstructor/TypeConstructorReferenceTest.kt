package org.purescript.psi.typeconstructor

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class TypeConstructorReferenceTest : BasePlatformTestCase() {

    fun `test finds usages from data declaration`() {
        myFixture.configureByText(
            "Main.purs",
            """
                module Data where
                data B = B
                data <caret>A = A
                func :: A -> A
                func a = a
                """.trimIndent()
        )
        val usageInfo = myFixture.testFindUsages("Main.purs")
        assertEquals(2, usageInfo.size)
    }

}
