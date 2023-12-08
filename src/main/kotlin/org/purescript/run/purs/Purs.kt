package org.purescript.run.purs

import com.google.gson.Gson
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.GeneralCommandLine.ParentEnvironmentType.CONSOLE
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.execution.util.ExecUtil
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.runBackgroundableTask
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.SystemInfo
import org.purescript.run.Npm

@Service(Service.Level.PROJECT)
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
            val output = CapturingProcessHandler(commandLine.withParameters("ide", "server"))
                    .runProcessWithProgressIndicator(it)
            started = false
            if (output.exitCode != 0) {
                error("purs ide server died with exit code ${output.exitCode}, and stderr:\n${output.stderr}")
            }
        }
    }

    private fun stopServer(path: String) =
            runBackgroundableTask(
                    "Stopping purs ide server ($path)",
                    project,
                    false
            ) {
                ExecUtil.execAndGetOutput(
                        commandLine
                                .withExePath(path)
                                .withParameters("ide", "client"),
                        Gson().toJson(mapOf("command" to "quit"))
                )
            }

    fun <T> withServer(function: () -> T): T {
        startServer()
        return function()
    }

    var path: String = commandName
        get() =
            project.service<PropertiesComponent>().getValue("purs path") ?: commandName
        set(value) {
            stopServer(field)
            PropertiesComponent.getInstance(project).setValue("purs path", value)
        }

    private val commandName: String
        get() = when {
            SystemInfo.isWindows -> "purs.cmd"
            else -> "purs"
        }

    val commandLine: GeneralCommandLine
        get() {
            val npm = project.service<Npm>()
            val pathEnv = npm.populatedPath
            return GeneralCommandLine(path)
                    .withCharset(charset("UTF8"))
                    .withWorkDirectory(project.basePath)
                    .withEnvironment("PATH", pathEnv)
                    .withParentEnvironmentType(CONSOLE)
        }

}