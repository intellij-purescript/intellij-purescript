package org.purescript.ide.refactoring

import com.intellij.lang.Language
import com.intellij.lang.refactoring.InlineActionHandler
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import org.purescript.PSLanguage
import org.purescript.psi.declaration.value.ValueDeclarationGroup

class PSInline : InlineActionHandler() {
    override fun isEnabledForLanguage(l: Language?): Boolean =
        l == PSLanguage

    override fun canInlineElement(element: PsiElement?): Boolean {
        return if (element is ValueDeclarationGroup) {
            element.valueDeclarations.singleOrNull()
                ?.namedBinders?.isEmpty()
                ?: false
        } else {
            false
        }
    }

    override fun inlineElement(project: Project, editor: Editor, element: PsiElement?) {
        when (element) {
            is ValueDeclarationGroup -> {
                InlineValueDeclarationGroup(project, element).run()
            }
        }
    }
}