package org.purescript.psi.expression

import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateExpressionSelectorBase
import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parents
import org.purescript.psi.declaration.value.ValueDecl

class ExpressionSelector : PostfixTemplateExpressionSelectorBase(null) {
    public override fun getNonFilteredExpressions(psi: PsiElement, doc: Document, offset: Int): MutableList<Expression> {
        val originalPsi = psi.parents(true).takeWhile { it !is ValueDecl }
        return originalPsi.filterIsInstance<Expression>().distinctBy { it.textRange }.toMutableList()
    }

    override fun getExpressions(context: PsiElement, document: Document, offset: Int): MutableList<Expression> {
        return super.getExpressions(context, document, offset).filterIsInstance<Expression>().toMutableList()
    }
}