package org.purescript.ide.purs

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.SystemInfo
import com.intellij.util.io.exists
import java.io.InputStream
import java.nio.file.Path
import java.util.concurrent.TimeUnit

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
        val npmProc: Process = when (cwd) {
            null -> ProcessBuilder(npmCmd).start()
            else -> ProcessBuilder(npmCmd).directory(cwd).start()
        }
        
        npmProc.waitFor(4, TimeUnit.SECONDS)
        
        return when (val output: InputStream? = npmProc.inputStream) {
            null -> ""
            else -> output.bufferedReader().readLine()
        }
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
