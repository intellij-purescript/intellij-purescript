package org.purescript.ide.fromatting

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.IncorrectOperationException
import java.util.*

class FormattingTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "test-data/formatting"

    fun testFormatter() {
        // purs-tidy format < Formatting.purs > FormattingFormatted.purs
        myFixture.configureByFile("Formatting.purs")
        
        val psiFile = myFixture.file
        val textRanges = listOf(psiFile.textRange)
        val codeStyleManager = CodeStyleManager.getInstance(project)
        val action = WriteCommandAction.writeCommandAction(project)
        
        action.run<IncorrectOperationException> {
            codeStyleManager.reformatText(psiFile, textRanges)
        }

        myFixture.checkResultByFile("FormattingFormatted.purs")
    }
}