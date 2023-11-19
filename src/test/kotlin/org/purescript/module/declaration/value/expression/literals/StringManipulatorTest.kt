package org.purescript.module.declaration.value.expression.literals

import com.intellij.openapi.application.runUndoTransparentWriteAction
import com.intellij.openapi.util.TextRange
import com.intellij.psi.ElementManipulators
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.purescript.getStringLiteral

class StringManipulatorTest : BasePlatformTestCase() {

    fun `test it handles empty single quote string`() {
        val string: PSStringLiteral = myFixture.configureByText(
            "Foo.purs",
            """
                |module Foo where
                |
                |string = ""
            """.trimMargin()
        ).getStringLiteral()

        val manipulator = ElementManipulators.getManipulator(string)
        runUndoTransparentWriteAction {
            manipulator.handleContentChange(string, "Hello World!")
        }

        myFixture.checkResult(
            "Foo.purs",
            """
                |module Foo where
                |
                |string = "Hello World!"
            """.trimMargin(),
            true
        )
    }

    fun `test it handles range in single quote string`() {
        val string: PSStringLiteral = myFixture.configureByText(
            "Foo.purs",
            """
                |module Foo where
                |
                |string = "Hello World!"
            """.trimMargin()
        ).getStringLiteral()

        val manipulator = ElementManipulators.getManipulator(string)
        runUndoTransparentWriteAction {
            val range = TextRange.allOf("World").shiftRight("\"Hello ".length)
            manipulator.handleContentChange(string, range, "You")
        }

        myFixture.checkResult(
            "Foo.purs",
            """
                |module Foo where
                |
                |string = "Hello You!"
            """.trimMargin(),
            true
        )
    }

    fun `test it handles range in triple quote string`() {
        val string: PSStringLiteral = myFixture.configureByText(
            "Foo.purs",
            """
                |module Foo where
                |
                |string = ${"\""}""Hello World!
                |${"\""}""
            """.trimMargin()
        ).getStringLiteral()

        val manipulator = ElementManipulators.getManipulator(string)
        runUndoTransparentWriteAction {
            val range = TextRange.allOf("World").shiftRight("""${"\""}""Hello """.length)
            manipulator.handleContentChange(string, range, "You")
        }

        myFixture.checkResult(
            "Foo.purs",
            """
                |module Foo where
                |
                |string = ${"\""}""Hello You!
                |${"\""}""
            """.trimMargin(),
            true
        )
    }
    fun `test it handles adding new line`() {
        val string: PSStringLiteral = myFixture.configureByText(
            "Foo.purs",
            """
                |module Foo where
                |
                |string = "Hello World!!
            """.trimMargin()
        ).getStringLiteral()

        val manipulator = ElementManipulators.getManipulator(string)
        runUndoTransparentWriteAction {
            val range = TextRange.allOf(" World").shiftRight("\"Hello".length)
            manipulator.handleContentChange(string, range, "\nYou")
        }

        myFixture.checkResult(
            "Foo.purs",
            """
                |module Foo where
                |
                |string = ${"\""}""Hello
                |You!${"\""}""
            """.trimMargin(),
            true
        )
    }
    fun `test it handles removing last new line`() {
        val string: PSStringLiteral = myFixture.configureByText(
            "Foo.purs",
            """
                |module Foo where
                |
                |string = ${"\""}""Hello
                |World!${"\""}""
            """.trimMargin()
        ).getStringLiteral()

        val manipulator = ElementManipulators.getManipulator(string)
        runUndoTransparentWriteAction {
            val range = TextRange.allOf("\nWorld").shiftRight("\"\"\"Hello".length)
            manipulator.handleContentChange(string, range, " You")
        }

        myFixture.checkResult(
            "Foo.purs",
            """
                |module Foo where
                |
                |string = "Hello You!"
            """.trimMargin(),
            true
        )
    }

}