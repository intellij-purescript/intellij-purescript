package org.purescript.ide.inspections

import com.intellij.codeInspection.LocalQuickFixOnPsiElement
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.purescript.psi.expression.PSParens

class RemoveParenthesis(element: PSParens): LocalQuickFixOnPsiElement(element) {
    override fun getFamilyName(): String = "Remove Parentheses"

    override fun getText(): String = "Remove Parentheses"

    override fun invoke(project: Project, file: PsiFile, startElement: PsiElement, endElement: PsiElement) {
        if (startElement != endElement) return
        when (startElement) {
            is PSParens -> startElement.value?.let { startElement.replace(it) }
        }
    }

}
