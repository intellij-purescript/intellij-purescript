package org.purescript.psi.expression

import com.intellij.codeInsight.template.postfix.templates.PostfixTemplate
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import org.purescript.psi.expression.controll.ifthenelse.IfPostfixTemplate

class ExpressionPostfixTemplateProvider : PostfixTemplateProvider {
    override fun getTemplates(): MutableSet<PostfixTemplate> = mutableSetOf(IfPostfixTemplate(this))
    override fun getPresentableName(): String = "Purescript Expression"
    override fun isTerminalSymbol(currentChar: Char): Boolean = currentChar == '.'
    override fun preExpand(file: PsiFile, editor: Editor): Unit = Unit
    override fun afterExpand(file: PsiFile, editor: Editor): Unit = Unit
    override fun preCheck(copyFile: PsiFile, realEditor: Editor, currentOffset: Int): PsiFile = copyFile
}