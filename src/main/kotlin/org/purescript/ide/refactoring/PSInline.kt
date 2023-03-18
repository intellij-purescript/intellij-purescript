package org.purescript.ide.refactoring

import com.intellij.ide.plugins.PluginManagerCore.isUnitTestMode
import com.intellij.lang.Language
import com.intellij.lang.refactoring.InlineActionHandler
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import org.purescript.PSLanguage
import org.purescript.module.declaration.value.ValueDeclarationGroup
import org.purescript.module.declaration.value.expression.ReplaceableWithInline
import org.purescript.module.declaration.value.expression.identifier.PSExpressionIdentifier
import org.purescript.psi.InlinableElement

class PSInline : InlineActionHandler() {
    override fun isEnabledForLanguage(l: Language?): Boolean =
        l == PSLanguage

    override fun canInlineElement(element: PsiElement?): Boolean =
        if (element is InlinableElement) element.canBeInlined() 
        else false

    override fun inlineElement(project: Project, editor: Editor, element: PsiElement?) {
        val document = editor.document
        val file = PsiDocumentManager.getInstance(project).getPsiFile(document)
        val offset = editor.caretModel.offset
        val original = file
            ?.findElementAt(offset)
            ?.parentOfType<PSExpressionIdentifier>(true)
        when (element) {
            is ValueDeclarationGroup -> {
                val dialog = InlineDialog(project, element, original) {
                    InlineProcessor(this) { usages ->
                        for (usage in usages) {
                            (usage.element as? ReplaceableWithInline)?.replaceWithInline(toInline)
                        }
                        // delete declaration
                        if (!isInlineThisOnly) toInline.deleteAfterInline()
                    }
                }
                if (!isUnitTestMode) {
                    dialog.show()
                } else {
                    try {
                        dialog.doAction()
                    } finally {
                        dialog.close(DialogWrapper.OK_EXIT_CODE, true)
                    }
                }
            }
        }
    }
}