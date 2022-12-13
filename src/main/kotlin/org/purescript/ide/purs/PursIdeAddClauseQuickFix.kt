package org.purescript.ide.purs

import com.google.gson.Gson
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.util.ExecUtil
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.application.runUndoTransparentWriteAction
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.progress.runBackgroundableTask
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Iconable
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import org.purescript.icons.PSIcons
import java.io.File

class PursIdeAddClauseQuickFix(private val textRange: TextRange) : IntentionAction, Iconable {
    data class Response(val result: List<String>, val resultType: String)

    override fun startInWriteAction(): Boolean = false
    override fun getText(): String = "Add clause"
    override fun getFamilyName(): String = "Purs ide suggestion"

    override fun isAvailable(
        project: Project,
        editor: Editor?,
        file: PsiFile?
    ): Boolean = true
    override fun invoke(project: Project, editor: Editor?, file: PsiFile) {
        val document =
            PsiDocumentManager.getInstance(file.project).getDocument(file)
                ?: return

        // without a purs bin path we can't annotate with it
        val pursBin = project.service<Npm>().pathFor("purs")
            ?: PathManager.findBinFile("purs")
            ?: return

        val gson = Gson()
        val tempFile: File =
            File.createTempFile("purescript-intellij", file.name)
        tempFile.writeText(file.text, file.virtualFile.charset)
        val request = mapOf(
            "command" to "addClause",
            "params" to mapOf(
                "line" to document.getText(textRange),
                "annotations" to true,
            )
        )
        runBackgroundableTask("Add Clause", project, false) {
            val output = ExecUtil.execAndGetOutput(
                GeneralCommandLine(pursBin.toString(), "ide", "client"),
                gson.toJson(request)
            )
            val response = gson.fromJson(output, Response::class.java)
            val lines = response.result.joinToString("\n")
            invokeLater {
                runUndoTransparentWriteAction {
                    document.replaceString(
                        textRange.startOffset,
                        textRange.endOffset,
                        lines
                    )
                }
            }
        }
    }

    override fun getIcon(flags: Int) = PSIcons.FILE
}
