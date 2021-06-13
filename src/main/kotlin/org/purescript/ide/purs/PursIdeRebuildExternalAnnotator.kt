package org.purescript.ide.purs

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.util.ExecUtil
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.ExternalAnnotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.util.SystemInfo.isWindows
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile


import org.jetbrains.annotations.NotNull
import java.io.File
import java.nio.file.Path

class PursIdeRebuildExternalAnnotator : ExternalAnnotator<PsiFile, Response>() {
    override fun collectInformation(file: PsiFile) = file

    override fun doAnnotate(file: PsiFile): Response? {
        val pursBin = getPursPath(file) ?: return null
        val gson = Gson()
        val tempFile: File =
            File.createTempFile("purescript-intellij", file.name)
        tempFile.writeText(file.text, file.virtualFile.charset)
        val request = mapOf(
            "command" to "rebuild",
            "params" to mapOf(
                "file" to tempFile.path,
                "actualFile" to file.virtualFile.path,
            )
        )
        val output = ExecUtil.execAndGetOutput(
            GeneralCommandLine(pursBin.toString(), "ide", "client"),
            gson.toJson(request)
        )
        return try {
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
        }
    }

    private fun getPursPath(file: PsiFile): Path? {
        val sequence = sequence<Path> {
            var tmp = file.virtualFile.toNioPath()
            while (tmp.parent != null) {
                yield(tmp.parent)
                tmp = tmp.parent
            }
        }
        val nodeModules = sequence
            .map { it.resolve("node_modules") }
            .firstOrNull { it.toFile().exists() }
            ?: return null
        val binDir = nodeModules.resolve(".bin")
        return when {
            isWindows -> binDir.resolve("purs.cmd")
            else -> binDir.resolve("purs")
        }
    }

    override fun apply(
        file: @NotNull PsiFile,
        annotationResult: Response,
        holder: @NotNull AnnotationHolder
    ) {
        val documentManager = PsiDocumentManager.getInstance(file.project)
        val document = documentManager.getDocument(file) ?: return
        val severity = when (annotationResult.resultType) {
            "error" -> HighlightSeverity.ERROR
            else -> HighlightSeverity.WARNING
        }
        for (result in annotationResult.result) {
            val textRange = result.position.textRange(document)
            val annotationBuilder = holder
                .newAnnotation(severity, result.message)
                .range(textRange)
                .needsUpdateOnTyping()
            if (result.suggestion != null) {
                val fix = PursIdeQuickFix(result.suggestion, result.message)
                annotationBuilder.withFix(fix)
            }
            annotationBuilder.create()
        }
    }


    override fun getPairedBatchInspectionShortName(): String {
        return PursIdeRebuildInspection.SHORT_NAME
    }

}