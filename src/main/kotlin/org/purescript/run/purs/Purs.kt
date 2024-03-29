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
import java.io.File
import kotlin.random.Random

@Service(Service.Level.PROJECT)
class Purs(val project: Project) {
    private var port:Int? = null;
    private fun startServer() {
        // don't start the activity under test, it will hang if it does
        if (PluginManagerCore.isUnitTestMode) return
        if (port != null) return
        port = Random.nextInt(15000, 16000)

        // without a project dir we don't know where to start the server
        val projectDir = project.guessProjectDir() ?: return
        val rootDir = projectDir.toNioPath()

        // without a purs bin path we can't annotate with it
        runBackgroundableTask("purs ide server ($path)", project, true) {
            val output = CapturingProcessHandler(commandLine.withParameters("ide", "server", "-p", port.toString()))
                    .runProcessWithProgressIndicator(it)
            port = null
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

    fun <T> withServer(function: (port: Int?) -> T): T {
        startServer()
        return function(port)
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
                    .withCharset(Charsets.UTF_8)
                    .withWorkDirectory(project.basePath)
                    .withEnvironment("PATH", pathEnv)
                    .withParentEnvironmentType(CONSOLE)
        }

}