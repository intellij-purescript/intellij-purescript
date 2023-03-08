package org.purescript.run.purs

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Iconable
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import org.purescript.icons.PSIcons

class PursIdeQuickFix(private val suggestion: Response.Suggestion, private val message: String) :
    IntentionAction, Iconable {
    override fun getIcon(flags: Int) = PSIcons.FILE
    override fun startInWriteAction() = true
    override fun getText() = message
    override fun getFamilyName() = "Purs ide suggestion"
    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?) = true
    override fun invoke(project: Project, editor: Editor?, file: PsiFile) {
        val documentManager = PsiDocumentManager.getInstance(file.project)
        val document = documentManager.getDocument(file) ?: return
        document.replaceString(
            suggestion.replaceRange.getStart(document),
            suggestion.replaceRange.getEnd(document),
            suggestion.replacement.replace("\n\n", "\n")
        )
    }
}