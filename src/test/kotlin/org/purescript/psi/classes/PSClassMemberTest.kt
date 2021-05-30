package org.purescript.psi.classes

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.purescript.getClassMember

class PSClassMemberTest : BasePlatformTestCase() {

    fun `test parses class member`() {
        val classMember = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                class Bar where
                    qux :: Int
            """.trimIndent()
        ).getClassMember()

        assertEquals("qux", classMember.name)
        assertEquals("Int", classMember.type.text)
    }

    fun `test parses class member with type variables`() {
        val classMember = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                class Bar a b where
                    qux :: a -> b
            """.trimIndent()
        ).getClassMember()

        assertEquals("qux", classMember.name)
        assertEquals("a -> b", classMember.type.text)
    }
}
