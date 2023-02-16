package org.purescript.run.spago

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.util.ExecUtil
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.progress.runBackgroundableTask
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.RootsChangeRescanningInfo
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.roots.SyntheticLibrary
import com.intellij.openapi.roots.impl.ProjectRootManagerImpl
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.purescript.icons.PSIcons
import java.io.IOException

@Service
class Spago(val project: Project) {
    var libraries = emptyList<SpagoLibrary>()

    init {
        updateLibraries()
        project.messageBus.connect().subscribe(
            VirtualFileManager.VFS_CHANGES,
            object : BulkFileListener {
                override fun after(events: List<VFileEvent>) {
                    for (event in events) {
                        val fileName = event.file?.name
                        when (fileName) {
                            "spago.yaml", "spago.dhall", "packages.dhall" -> {
                                updateLibraries()
                            }
                        }
                    }
                }
            })
    }

    private fun updateLibraries() =
        runBackgroundableTask("Spago", project, true) {
            val commandName = when {
                SystemInfo.isWindows -> "spago.cmd"
                else -> "spago"
            }
            val commandLine = GeneralCommandLine(commandName, "ls", "deps", "--json")
                .withWorkDirectory(project.basePath)
                .withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
            val lines = try {
                ExecUtil.execAndGetOutput(commandLine, "").split("\n")
            } catch (e: ExecutionException) {
                return@runBackgroundableTask
            }
            val libraries = mutableListOf<SpagoLibrary>()
            for (line in lines) {
                if (line.isEmpty()) continue
                val dep = try {
                    Json.decodeFromString<Dep>(line)
                } catch (e: SerializationException) {
                    continue
                }
                when (dep.repo.tag) {
                    "Remote" -> {
                        val src = project
                            .guessProjectDir()
                            ?.findFileByRelativePath(".spago/${dep.packageName}/${dep.version}/src")
                        val roots = src?.let { mutableListOf(it) } ?: mutableListOf()
                        libraries.add(
                            SpagoLibrary(
                                dep.packageName,
                                dep.version,
                                roots
                            )
                        )
                    }

                    "Local" -> {
                        val src: VirtualFile? = project
                            .guessProjectDir()
                            ?.findFileByRelativePath("${dep.repo.contents}/src")
                        val roots: MutableList<VirtualFile> = src?.let { mutableListOf(it) } ?: mutableListOf()
                        libraries.add(
                            SpagoLibrary(
                                dep.packageName,
                                dep.version,
                                roots
                            )
                        )
                    }
                }
            }
            this.libraries = libraries

            invokeLater {
                runWriteAction {
                    val rootManagerImpl = ProjectRootManagerImpl.getInstanceImpl(project)
                    rootManagerImpl.makeRootsChange({}, RootsChangeRescanningInfo.TOTAL_RESCAN)
                }
            }
        }

    @Serializable
    data class Dep(val packageName: String, val version: String, val repo: Repo)

    @Serializable
    data class Repo(val contents: String, val tag: String)

    data class SpagoLibrary(val packageName: String, val version: String, val sourceRoots: MutableList<VirtualFile>) :
        SyntheticLibrary(), ItemPresentation {
        override fun getSourceRoots(): MutableCollection<VirtualFile> {
            return sourceRoots
        }

        override fun getPresentableText() = "$packageName:$version"

        override fun getIcon(unused: Boolean) = PSIcons.SPAGO

    }
}