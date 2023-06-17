package org.purescript.run

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.GeneralCommandLine.ParentEnvironmentType.CONSOLE
import com.intellij.execution.util.ExecUtil
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.SystemInfo
import java.nio.file.Path
import kotlin.io.path.exists

@Service
class Npm(val project: Project) {

    val localBinPath: String? = project.basePath?.let {
        Path.of("$it/node_modules/.bin").toString()
    } 
    val globalBinPath: String? by lazy { run("npm config get prefix") }

    private fun run(command: String): String? {
        val npmCmd = when {
            SystemInfo.isWindows -> listOf("cmd", "/c", command)
            else -> listOf("/usr/bin/env", "sh", "-c", command)
        }
        
        val cwd = try {
            project.guessProjectDir()?.toNioPath()?.toFile()
        } catch (_:UnsupportedOperationException) {
            null
        }
        val commandLine = GeneralCommandLine(npmCmd)
            .withParentEnvironmentType(CONSOLE)
            .withWorkDirectory(cwd)
        return ExecUtil.execAndReadLine(commandLine)
    }

    private val log = logger<Npm>()

    fun pathFor(command: String): Path? {
        val binary = when {
            SystemInfo.isWindows -> "$command.cmd"
            else -> command
        }
        
        val localCommand = localBinPath?.let { Path.of(it, binary) }
        if (localCommand != null) {
            if (localCommand.exists()) return localCommand
        }

        val globalCommand = globalBinPath?.let { Path.of(it, binary) }
        if (globalCommand != null) {
            if (globalCommand.exists()) return globalCommand
        }

        if (log.isDebugEnabled) log.debug("$command is not found")
        return null
    }
}
