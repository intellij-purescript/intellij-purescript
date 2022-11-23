package org.purescript.ide.purs

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.SystemInfo
import java.nio.file.Path
import java.util.concurrent.TimeUnit

class Npm {
    companion object {
        private val linuxGetNPMRootShellCmd =
            listOf("/usr/bin/env", "bash", "-c", "npm -g root")
        private val windowsGetNPMRootShellCmd =
            listOf("cmd", "/c", "npm -g root")
        private val log = logger<Npm>()
        private var paths: MutableMap<String, Path> = mutableMapOf()

        fun pathFor(project: Project, command: String): Path? {
            if (command !in paths) {
                val virtualFile = project.guessProjectDir()
                if (virtualFile != null) {
                    try {
                        val toNioPath = virtualFile.toNioPath()
                        localVersion(toNioPath, command)?.let {
                            paths[command] = it
                        }
                    } catch (e:UnsupportedOperationException) {
                        
                    }
                }
            }
            if (command !in paths) {
                globalVersion(command)?.let {
                    paths[command] = it
                }
            }
            if (log.isDebugEnabled) {
                if (command in paths) {
                    log.debug("$command is not found")
                } else {
                    log.debug("$command is found at ${paths[command]}")
                }
            }
            return paths[command]
        }

        fun localVersion(rootDir: Path, command: String): Path? {
            val sequence: Sequence<Path> = sequence {
                var localPath: Path? = rootDir
                while (localPath != null) {
                    yield(localPath)
                    localPath = localPath.parent
                }
            }
            return extractPursCmd(
                sequence,
                "node_modules/.bin",
                nameToCommand(command)
            )
        }

        fun globalVersion(command: String): Path? {
            val globalSequence = sequence<Path> {
                var globalPath: Path? = npmGlobalModulesDir()
                while (globalPath != null) {
                    yield(globalPath)
                    globalPath = globalPath.parent
                }
            }
            return extractPursCmd(
                globalSequence,
                "./",
                nameToCommand(command)
            )
        }

        private fun extractPursCmd(
            sequence: Sequence<Path>,
            pursExecutablePathStr: String,
            command: String
        ): Path? {
            return sequence.map { it.resolve(pursExecutablePathStr) }
                .filter { it.toFile().exists() }
                .map { it.resolve(command) }
                .firstOrNull { it.toFile().exists() }
        }

        private fun nameToCommand(cmd: String) = when {
            SystemInfo.isWindows -> "$cmd.cmd"
            else -> "purescript/$cmd.bin"
        }

        fun npmGlobalModulesDir(): Path {
            val npmCmd = when {
                SystemInfo.isWindows -> windowsGetNPMRootShellCmd
                else -> linuxGetNPMRootShellCmd
            }

            val npmProc = ProcessBuilder(npmCmd)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .start()

            npmProc.waitFor(4, TimeUnit.SECONDS)
            val rawPath = npmProc.inputStream.bufferedReader().readLine()
            return when {
                !rawPath.isNullOrEmpty() -> Path.of(rawPath)
                else -> Path.of("/")
            }
        }
    }
}
