package org.purescript.run.purs


import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.util.ExecUtil
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.ExternalAnnotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.components.service
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import java.io.File

class PursIdeRebuildExternalAnnotator : ExternalAnnotator<PsiFile, Response>() {
    override fun collectInformation(file: PsiFile) = file

    override fun doAnnotate(file: PsiFile): Response? {

        // without a purs bin path we can't annotate with it
        val project = file.project
        val purs = project.service<Purs>()
        val tempFile: File =
            File.createTempFile("purescript-intellij", file.name)
        tempFile.writeText(file.text, file.virtualFile.charset)
        val filePath = file.virtualFile.toNioPath()
        val jsPath = filePath.parent.resolve((filePath.toFile().nameWithoutExtension + ".js"))
        val jsFile = VirtualFileManager.getInstance().findFileByNioPath(jsPath)
        val tempJsFile: File?
        if (jsFile != null) {
            tempJsFile = (tempFile.toPath().parent.resolve((tempFile.nameWithoutExtension + ".js"))).toFile()
            tempJsFile?.writeBytes(jsFile.contentsToByteArray())
        } else {
            tempJsFile = null
        }
        val gson = Gson()
        return purs.withServer {
            val output = ExecUtil.execAndGetOutput(
                GeneralCommandLine(
                    purs.path,
                    "ide",
                    "client"
                ),
                gson.toJson(
                    mapOf(
                        "command" to "rebuild",
                        "params" to mapOf(
                            "file" to tempFile.path,
                            "actualFile" to file.virtualFile.path,
                        )
                    )
                )
            )
            try {
                val json = gson.fromJson(output, Response::class.java)
                if (json?.resultType in listOf("success", "error")) {
                    json
                } else {
                    null
                }
            } catch (e: JsonSyntaxException) {
                null
            } finally {
                tempFile.delete()
                tempJsFile?.delete()
            }
        }
    }

    override fun apply(
        file: PsiFile,
        annotationResult: Response,
        holder: AnnotationHolder
    ) {
        val documentManager = PsiDocumentManager.getInstance(file.project)
        val document = documentManager.getDocument(file) ?: return
        val severity = when (annotationResult.resultType) {
            "error" -> HighlightSeverity.ERROR
            else -> HighlightSeverity.WARNING
        }
        for (result in annotationResult.result) {
            if (result.errorCode == "UnusedDeclaration") continue
            if (result.errorCode == "UnusedImport") continue
            if (result.errorCode == "UnusedExplicitImport") continue
            val textRange = result.position.textRange(document)
            val annotationBuilder = holder
                .newAnnotation(severity, "Purs ide rebuild: ${result.errorCode}")
                .tooltip(result.message)
                .range(textRange)
                .needsUpdateOnTyping()
            if (result.suggestion != null) {
                val fix = PursIdeQuickFix(result.suggestion, result.message)
                annotationBuilder.withFix(fix)
            }
            when (result.errorCode) {
                "OrphanTypeDeclaration" -> {
                    val fix = PursIdeAddClauseQuickFix(textRange)
                    annotationBuilder.withFix(fix)
                }
            }
            annotationBuilder.create()
        }
    }

    override fun getPairedBatchInspectionShortName(): String {
        return PursIdeRebuildInspection.SHORT_NAME
    }
}