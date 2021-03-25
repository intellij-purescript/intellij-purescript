package org.purescript.psi.data

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.purescript.file.PSFile

class DataDeclarationReferenceTest : BasePlatformTestCase(){

    fun testDataCanFindUsages() {
        myFixture.configureByText(
            "Main.purs",
            """
                module Data where
                data B = B
                data <caret>A = A
                func :: A -> A
                func a = a
                """.trimIndent()
        ) as PSFile
        val usageInfo = myFixture.testFindUsages("Main.purs")
        assertEquals(2, usageInfo.size)
    }

}