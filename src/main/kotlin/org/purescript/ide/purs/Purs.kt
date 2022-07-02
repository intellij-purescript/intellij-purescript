package org.purescript.ide.purs

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.SystemInfo
import java.nio.file.Path
import java.util.concurrent.TimeUnit

class Purs {
    companion object {
        private val linuxGetNPMRootShellCmd = listOf<String>("/usr/bin/env", "bash", "-c", "npm -g root")
        private val windowsGetNPMRootShellCmd = listOf<String>("cmd", "/c", "npm -g root")
        private val log = logger<Purs>()
        private var pursCmd: Path? = null

        fun nodeModulesVersion(project: Project): Path? {
            if (pursCmd == null) {
                val projectDir = project.guessProjectDir() ?: return null
                val rootDir = projectDir.toNioPath()

                val sequence: Sequence<Path> = sequence {
                    var localPath: Path? = rootDir
                    while (localPath != null) {
                        yield(localPath)
                        localPath = localPath.parent
                    }
                }
                pursCmd = extractPursCmd(sequence, "node_modules/.bin", SystemInfo.isWindows)

                if (pursCmd == null) {
                    val globalSequence = sequence<Path> {
                        var globalPath: Path? = npmGlobalModulesDir()
                        while (globalPath != null) {
                            yield(globalPath)
                            globalPath = globalPath.parent
                        }
                    }
                    pursCmd = extractPursCmd(globalSequence, "./", SystemInfo.isWindows)
                }
            }

            if (log.isDebugEnabled) {
                if (pursCmd == null) {
                    log.debug("purs is not found")
                } else {
                    log.debug("purs is found at $pursCmd")
                }
            }
            return pursCmd
        }

        private fun extractPursCmd(sequence: Sequence<Path>, pursExecutablePathStr: String, isWindows: Boolean): Path? {
            return sequence.map { it.resolve(pursExecutablePathStr) }
                .filter { it.toFile().exists() }.map {
                    if (isWindows) {
                        it.resolve("purs.cmd")
                    } else {
                        it.resolve("purescript/purs.bin")
                    }
                }.firstOrNull { it.toFile().exists() }
        }

        fun npmGlobalModulesDir(): Path {
            val npmCmd = if (SystemInfo.isWindows) windowsGetNPMRootShellCmd
            else linuxGetNPMRootShellCmd

            val npmProc = ProcessBuilder(npmCmd).redirectError(ProcessBuilder.Redirect.PIPE)
                .redirectOutput(ProcessBuilder.Redirect.PIPE).start()

            npmProc.waitFor(4, TimeUnit.SECONDS)
            val rawPath: String? = npmProc.inputStream.bufferedReader().readLine()
            return if (rawPath != null && rawPath.isNotEmpty()) {
                Path.of(rawPath)
            } else Path.of("/")
        }
    }
}
