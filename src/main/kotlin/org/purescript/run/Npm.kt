package org.purescript.run

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.GeneralCommandLine.ParentEnvironmentType.CONSOLE
import com.intellij.execution.util.ExecUtil
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.SystemInfo
import kotlin.io.path.exists
import java.nio.file.Path

@Service
class Npm(val project: Project) {

    private val localBinPath: String by lazy { run("npm bin") }

    private val globalBinPath: String by lazy { run("npm -g bin") }

    private fun run(command: String): String {
        val npmCmd = when {
            SystemInfo.isWindows -> listOf("cmd", "/c", command)
            else -> listOf("/usr/bin/env", "bash", "-c", command)
        }
        
        val cwd = try {
            project.guessProjectDir()?.toNioPath()?.toFile()
        } catch (_:UnsupportedOperationException) {
            null
        }
        val commandLine = GeneralCommandLine(npmCmd)
            .withParentEnvironmentType(CONSOLE)
            .withWorkDirectory(cwd)
        return ExecUtil.execAndReadLine(commandLine) ?: ""
    }

    private val log = logger<Npm>()

    fun pathFor(command: String): Path? {
        val binary = when {
            SystemInfo.isWindows -> "$command.cmd"
            else -> command
        }
        val localCommand = Path.of(localBinPath, binary)
        if (localCommand.exists()) return localCommand

        val globalCommand = Path.of(globalBinPath, binary)
        if (globalCommand.exists()) return globalCommand

        if (log.isDebugEnabled) log.debug("$command is not found")
        return null
    }
}
