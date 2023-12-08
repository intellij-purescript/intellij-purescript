package org.purescript.run.purs


import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.ExternalAnnotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.application.runWriteActionAndWait
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.Socket
import java.util.regex.Pattern


class PursIdeRebuildExternalAnnotator : ExternalAnnotator<PsiFile, Response>() {
    override fun getPairedBatchInspectionShortName(): String = PursIdeRebuildInspection.SHORT_NAME
    override fun collectInformation(file: PsiFile) = file

    override fun doAnnotate(file: PsiFile?): Response? {
        if (file == null) return null

        // without a purs bin path we can't annotate with it
        val project = file.project
        val purs = project.service<Purs>()
        val filePath = file.virtualFile.toNioPath()

        // Make sure that the corresponding FFI file on disk is up-to-date
        val jsPath = filePath.parent.resolve((filePath.toFile().nameWithoutExtension + ".js"))
        VirtualFileManager.getInstance().findFileByNioPath(jsPath)?.let { jsFile ->
            runWriteActionAndWait {
                val fdm = FileDocumentManager.getInstance()
                fdm.getDocument(jsFile)?.let { fdm.saveDocument(it) }
            }
        }

        val gson = GsonBuilder().disableHtmlEscaping().create()

        return purs.withServer { port ->
            if (port == null) {
                error("No PSC ide port")
                return@withServer null
            }
            val payload = Json.encodeToString(PursCommand("rebuild", PursParams("data:"+file.text, file.virtualFile.path))) + "\n"
            val socket = Socket("localhost", port)
            socket.outputStream.write(payload.toByteArray(Charsets.UTF_8))
            val output = socket.inputStream.bufferedReader(Charsets.UTF_8).readLine()

            try {
                val json = gson.fromJson(output, Response::class.java)
                if (json?.resultType in listOf("success", "error")) {
                    json
                } else {
                    null
                }
            } catch (e: JsonSyntaxException) {
                null
            }
        }
    }


    @Serializable
    data class PursCommand(val command: String, val params: PursParams)
    @Serializable
    data class PursParams(val file:String, val actualFile:String)

    override fun apply(file: PsiFile, annotationResult: Response?, holder: AnnotationHolder) {
        if (annotationResult == null) return
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
            if (result.errorCode == "ModuleNotFound") continue
            val textRange = result.position.textRange(document)
                    .let { if (it.length <= 0) it.grown(1).shiftLeft(1) else it }
            val annotationBuilder = holder
                    .newAnnotation(severity, "Purs ide rebuild: ${result.errorCode}")
                    .tooltip(limitTooltip(result.message))
                    .range(textRange)
                    .needsUpdateOnTyping()
            if (result.suggestion != null) {
                val name = result.errorCode
                        ?.split(Pattern.compile("(?=[A-Z])"))
                        ?.joinToString(" ")
                val fix = PursIdeQuickFix(result.suggestion, name ?: result.message)
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

    // IntelliJ gets very slow on large tooltips
    private fun limitTooltip(message: String): String {

        val lines = message.lines()
        val maxLinesToShow = 50
        val maxColumnWidth = 120
        val tooltipLines =
                if (lines.size < maxLinesToShow) lines
                else {
                    val separatorHint = "<br />[... ${lines.size - maxLinesToShow} more lines elided]<br />"
                    lines.take(35) + listOf(separatorHint) + lines.takeLast(15)
                }
        return tooltipLines.joinToString("<br />") { it.take(maxColumnWidth) }
    }
}