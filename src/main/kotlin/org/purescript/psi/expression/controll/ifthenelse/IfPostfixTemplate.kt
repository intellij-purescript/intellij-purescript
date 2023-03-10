package org.purescript.psi.expression.controll.ifthenelse

import com.intellij.codeInsight.template.Template
import com.intellij.codeInsight.template.impl.TextExpression
import com.intellij.codeInsight.template.postfix.templates.StringBasedPostfixTemplate
import com.intellij.psi.PsiElement
import org.purescript.psi.expression.ExpressionPostfixTemplateProvider
import org.purescript.psi.expression.ExpressionSelector


class IfPostfixTemplate(templateProvider: ExpressionPostfixTemplateProvider) :
    StringBasedPostfixTemplate(
        ".if", ".if", "",
        ExpressionSelector(),
        templateProvider
    ) {
    override fun getTemplateString(element: PsiElement) =
        "if \$END$ then \$EXPR1$ else \$EXPR2$"

    override fun setVariables(template: Template, element: PsiElement) {
        super.setVariables(template, element)
        template.addVariable("END", null, TextExpression("true"),  true)
        template.addVariable("EXPR1", null, TextExpression(element.text),  false)
        template.addVariable("EXPR2", null, TextExpression(element.text),  false)
    }

    override fun getElementToRemove(expr: PsiElement?): PsiElement? = expr
}


