package org.purescript.module.declaration.classes

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.purescript.getClassDeclaration

class PSClassMemberListTest : BasePlatformTestCase() {

    fun `test parses class members`() {
        val classMemberList = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                class Bar where
                    foo :: Int
                    bar :: Int -> Boolean
            """.trimIndent()
        ).getClassDeclaration().classMemberList!!

        assertSize(2, classMemberList.classMembers)
    }
}
