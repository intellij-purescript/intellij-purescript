package org.purescript.psi.expression

import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateExpressionSelectorBase
import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parents
import org.purescript.psi.declaration.value.ValueDecl

class ExpressionSelector : PostfixTemplateExpressionSelectorBase({
    when (it) {
        is ExpressionAtom, is PSValue -> true
        else -> false
    }
}) {
    override fun getNonFilteredExpressions(psi: PsiElement, doc: Document, offset: Int): MutableList<PsiElement> {
        val originalPsi = psi.parents(true).takeWhile { it !is ValueDecl }
        return originalPsi.toMutableList()
    }
}