package org.purescript.ide.purs

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.util.SystemInfo
import com.intellij.util.io.exists
import java.nio.file.Path
import java.util.concurrent.TimeUnit


class Npm {
    companion object {
        private val localBinPath: String by lazy { run("npm bin") }

        private val globalBinPath: String by lazy { run("npm -g bin") }

        private fun run(command: String): String {
            val npmCmd = when {
                SystemInfo.isWindows -> listOf("cmd", "/c", command)
                else -> listOf("/usr/bin/env", "bash", "-c", command)
            }
            val npmProc = ProcessBuilder(npmCmd)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .start()
            npmProc.waitFor(4, TimeUnit.SECONDS)
            return npmProc.inputStream.bufferedReader().readLine()
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
}
