package org.purescript.ide.purs

import com.google.gson.Gson
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.execution.util.ExecUtil
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.runBackgroundableTask
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir

@Service
class Purs(val project: Project) {
    private var started = false
    private fun startServer() {
        // don't start the activity under test, it will hang if it doese
        if (PluginManagerCore.isUnitTestMode) return
        if (started) return
        started = true

        // without a project dir we don't know where to start the server
        val projectDir = project.guessProjectDir() ?: return
        val rootDir = projectDir.toNioPath()

        // without a purs bin path we can't annotate with it
        runBackgroundableTask("purs ide server ($path)", project, true) {
            CapturingProcessHandler(
                GeneralCommandLine(path, "ide", "server")
                    .withWorkDirectory(rootDir.toFile())
            ).runProcessWithProgressIndicator(it)
            started = false
        }
    }

    private fun stopServer(path: String) =
        runBackgroundableTask(
            "Stopping purs ide server ($path)",
            project,
            false
        ) {
            ExecUtil.execAndGetOutput(
                GeneralCommandLine(path, "ide", "client"),
                Gson().toJson(mapOf("command" to "quit"))
            )
        }

    fun <T> withServer(function: () -> T): T {
        startServer()
        return function()
    }

    var path: String = project.service<Npm>().pathFor("purs")?.toString() ?: ""
        get() =
            project.service<PropertiesComponent>().getValue("purs path")
                ?: project.service<Npm>().pathFor("purs")?.toString() ?: ""
        set(value) {
            stopServer(field)
            PropertiesComponent
                .getInstance(project)
                .setValue("purs path", value)
        }


}