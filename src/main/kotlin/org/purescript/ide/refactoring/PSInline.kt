package org.purescript.ide.refactoring

import com.intellij.ide.plugins.PluginManagerCore.isUnitTestMode
import com.intellij.lang.Language
import com.intellij.lang.refactoring.InlineActionHandler
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentsOfType
import org.purescript.PSLanguage
import org.purescript.module.declaration.value.expression.ReplaceableWithInline
import org.purescript.psi.InlinableElement

class PSInline : InlineActionHandler() {
    override fun isEnabledForLanguage(l: Language?): Boolean = l == PSLanguage
    override fun canInlineElement(e: PsiElement?): Boolean = (e as? InlinableElement)?.canBeInlined() ?: false
    override fun inlineElement(project: Project, editor: Editor, element: PsiElement?) {
        if (element is InlinableElement && element.canBeInlined()) {
            val dialog = InlineDialog(project, element, getOriginal(editor, project, element)) {
                InlineProcessor(this) { usages ->
                    for (usage in usages) (usage.element as? ReplaceableWithInline)?.replaceWithInline(toInline)
                    if (!isInlineThisOnly) toInline.deleteAfterInline()
                }
            }
            if (!isUnitTestMode) dialog.show()
            else try {
                dialog.doAction()
            } finally {
                dialog.close(DialogWrapper.OK_EXIT_CODE, true)
            }
        }
    }

    private fun getOriginal(editor: Editor, project: Project, element: PsiElement): ReplaceableWithInline? {
        val document = editor.document
        val file = PsiDocumentManager.getInstance(project).getPsiFile(document) ?: return null
        val offset = editor.caretModel.offset
        val elementAtOffset = file.findElementAt(offset) ?: return null
        return elementAtOffset.parentsOfType<ReplaceableWithInline>(withSelf = true)
            .firstOrNull { it.reference?.resolve() == element } }
}