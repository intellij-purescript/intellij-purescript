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
    val globalBinPath: String? by lazy {
        val npmCmd = when {
            SystemInfo.isWindows -> listOf("cmd", "/c", "npm config get prefix")
            else -> listOf("/usr/bin/env", "npm", "config", "get", "prefix")
        }
        val cwd = try {
            project.guessProjectDir()?.toNioPath()?.toFile()
        } catch (_: UnsupportedOperationException) {
            null
        }
        val commandLine = GeneralCommandLine(npmCmd)
            .withParentEnvironmentType(CONSOLE)
            .withWorkDirectory(cwd)
        ExecUtil.execAndReadLine(commandLine)
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
