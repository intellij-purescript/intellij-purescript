package org.purescript.psi.expression

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class ExpressionIdentifierCompletionContributorTest: BasePlatformTestCase() {
    
    fun `test find values in other files with only one complete`() {
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
        myFixture.testCompletionVariants("Foo.purs", "y0", "y1", "y2")
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

        myFixture.complete(CompletionType.BASIC, 1)
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
    fun `test imports only once`() {
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
                |module Foo where
                |
                |import Bar (x1) as Bar
                |
                |y0 = Bar.x<caret>
            """.trimMargin()
        )

        myFixture.complete(CompletionType.BASIC, 3)
        myFixture.checkResult(
            "Foo.purs",
            """
                |module Foo where
                |
                |import Bar (x1) as Bar
                |
                |y0 = Bar.x
            """.trimMargin(), true)
    }
    fun `test reuse imports`() {
        myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                
                y2 = 1
                x1 = 1
            """.trimIndent()
        )
        myFixture.configureByText(
            "Foo.purs",
            """
                |module Foo where
                |
                |import Bar (y2) as Bar
                |
                |y0 = Bar.x<caret>
            """.trimMargin()
        )

        myFixture.complete(CompletionType.BASIC, 1)
        myFixture.checkResult(
            "Foo.purs",
            """
                |module Foo where
                |
                |import Bar (x1, y2) as Bar
                |
                |y0 = Bar.x1
            """.trimMargin(), true)
    }
    
    fun `test imports with namespace if qualified for operators`() {
        myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                
                infix 0 foo as ++
            """.trimIndent()
        )
        myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                
                y0 = 1 Bar.+<caret>
            """.trimIndent()
        )

        myFixture.complete(CompletionType.BASIC, 1)
        myFixture.checkResult(
            "Foo.purs",
            """
                |module Foo where
                |
                |import Bar ((++)) as Bar
                |
                |y0 = 1 Bar.++
            """.trimMargin(), true)
    }
    fun `test imports with namespace for newtype`() {
        myFixture.configureByText(
            "Bar.purs",
            """
                |module Bar where
                |
                |newtype User = User String
            """.trimMargin()
        )
        myFixture.configureByText(
            "Foo.purs",
            """
                |module Foo where
                |
                |y0 = Bar.User<caret>
            """.trimMargin()
        )

        myFixture.complete(CompletionType.BASIC, 2)
        myFixture.checkResult(
            "Foo.purs",
            """
                |module Foo where
                |
                |import Bar (User(User)) as Bar
                |
                |y0 = Bar.User
            """.trimMargin(), true)
    }
    fun `failing test imports with namespace for newtype only if constructor is exported`() {
        myFixture.configureByText(
            "Bar.purs",
            """
                |module Bar (User) where
                |
                |newtype User = User String
            """.trimMargin()
        )
        myFixture.configureByText(
            "Foo.purs",
            """
                |module Foo where
                |
                |y0 = Bar.User<caret>
            """.trimMargin()
        )

        myFixture.complete(CompletionType.BASIC, 3)
        myFixture.checkResult(
            "Foo.purs",
            """
                |module Foo where
                |
                |y0 = Bar.User
            """.trimMargin(), true)
    }
}