package org.purescript.ide.formatting

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessAdapter
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessEvent
import com.intellij.formatting.service.AsyncDocumentFormattingService
import com.intellij.formatting.service.AsyncFormattingRequest
import com.intellij.formatting.service.FormattingService
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.components.service
import com.intellij.openapi.util.NlsSafe
import com.intellij.psi.PsiFile
import org.purescript.file.PSFile
import org.purescript.ide.purs.Npm
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.ExecutionException

class PSExternalFormatter : AsyncDocumentFormattingService() {
    override fun createFormattingTask(request: AsyncFormattingRequest): FormattingTask? {

        // without a purs bin path we can't annotate with it
        val project = request.context.project
        val pursTidyBin = project.service<Npm>().pathFor("purs-tidy") 
            ?: PathManager.findBinFile("purs-tidy")
            ?: return null
        val params = listOf("format")
        return try {
            val commandLine = GeneralCommandLine()
                .withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
                .withExePath(pursTidyBin.toString())
                .withParameters(params)
                .withInput(request.ioFile)
                .withCharset(StandardCharsets.UTF_8)
            val handler = OSProcessHandler(commandLine)
            val listener = object : CapturingProcessAdapter() {
                override fun processTerminated(event: ProcessEvent) =
                    when (event.exitCode) {
                        0 -> request.onTextReady(output.stdout)
                        else -> request.onError(
                            "Purs Tidy failed", output.stderr
                        )
                    }
            }
            return object : FormattingTask {
                override fun run() {
                    handler.addProcessListener(listener)
                    handler.startNotify()
                }

                override fun cancel(): Boolean {
                    handler.destroyProcess()
                    return true
                }

                override fun isRunUnderProgress(): Boolean = true
            }
        } catch (e: ExecutionException) {
            request.onError("Purs Tidy failed", e.message?: "")
            null
        }
    }

    override fun getNotificationGroupId(): String {
        return "Purescript"
    }

    @Suppress("UnstableApiUsage")
    override fun getName(): @NlsSafe String {
        return "Purs Tidy"
    }

    override fun getFeatures(): Set<FormattingService.Feature> {
        return EnumSet.noneOf(FormattingService.Feature::class.java)
    }

    override fun canFormat(file: PsiFile): Boolean {
        return file is PSFile.Psi
    }
}