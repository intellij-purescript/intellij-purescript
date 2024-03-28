package org.purescript.run.spago

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.GeneralCommandLine.ParentEnvironmentType.CONSOLE
import com.intellij.execution.util.ExecUtil
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.runBackgroundableTask
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.RootsChangeRescanningInfo
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.roots.SyntheticLibrary
import com.intellij.openapi.roots.impl.ProjectRootManagerImpl
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import org.purescript.icons.PSIcons
import org.purescript.run.Npm

@Service(Service.Level.PROJECT)
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

    val commandLine: GeneralCommandLine
        get() {
            val commandName = when {
                SystemInfo.isWindows -> "spago.cmd"
                else -> "spago"
            }
            val npm = project.service<Npm>()
            val pathEnv = npm.populatedPath
            return GeneralCommandLine(commandName)
                .withCharset(Charsets.UTF_8)
                .withWorkDirectory(project.basePath)
                .withEnvironment("PATH", pathEnv)
                .withParentEnvironmentType(CONSOLE)
        }

    private fun updateLibraries() =
        runBackgroundableTask("Spago", project, true) {
            val commandLine = this.commandLine.withParameters("ls", "deps", "--transitive", "--json")
            val jsonRaw = try {
                ExecUtil.execAndGetOutput(commandLine, "")
            } catch (e: ExecutionException) {
                return@runBackgroundableTask
            }
            // spago next
            runCatching {
                Json.decodeFromString<Map<String, NextDep>>(jsonRaw)
            }.onSuccess { depMap ->
                val projectDir = project.guessProjectDir()
                libraries = depMap.mapNotNull { (name, dep) ->
                    val (path, version) = when (dep) {
                        is NextDep.Registry -> {
                            val version = dep.version
                            ".spago/p/$name-$version/src" to "v$version"
                        }
                        is NextDep.Local -> "${dep.path}/src" to "local"
                    }
                    val root = projectDir?.findFileByRelativePath(path) ?: LocalFileSystem.getInstance().findFileByPath(path)
                    val roots = listOfNotNull(root).toMutableList()
                    SpagoLibrary(name, version, roots)
                }
                triggerRootsChanged()
                return@runBackgroundableTask
            }
            // spago legacy
            val lines = jsonRaw.split("\n")
            val libraries = mutableListOf<SpagoLibrary>()
            for (line in lines) {
                if (line.isEmpty()) continue
                val dep = try {
                    Json.decodeFromString<Dep>(line)
                } catch (e: SerializationException) {
                    continue
                }
                val projectDir = project.guessProjectDir()
                val relativePath = when (dep.repo.tag) {
                    "Remote" -> ".spago/${dep.packageName}/${dep.version}/src"
                    "Local" -> "${dep.repo.contents}/src"
                    else -> null
                }
                val root = relativePath?.let { projectDir?.findFileByRelativePath(it) }
                val roots = listOfNotNull(root).toMutableList()
                libraries.add(SpagoLibrary(dep.packageName, dep.version, roots))
            }
            this.libraries = libraries
            triggerRootsChanged()
        }

    private fun triggerRootsChanged() = invokeLater {
        runWriteAction {
            val rootManagerImpl = ProjectRootManagerImpl.getInstanceImpl(project)
            rootManagerImpl.makeRootsChange({}, RootsChangeRescanningInfo.TOTAL_RESCAN)
        }
    }

    @Serializable(NextDepDeserializer::class)
    sealed interface NextDep {
        data class Registry(val version: String): NextDep
        data class Local(val path: String): NextDep
    }

    object NextDepDeserializer: KSerializer<NextDep> {
        override val descriptor: SerialDescriptor = buildClassSerialDescriptor("NextDep") {
            element("type", String.serializer().descriptor)
            element("value", NextValueSurrogate.serializer().descriptor)
        }
        override fun serialize(encoder: Encoder, value: NextDep) {
            val surrogate = when(value) {
                is NextDep.Registry -> NextDepSurrogate("registry", NextValueSurrogate(version = value.version))
                is NextDep.Local -> NextDepSurrogate("local", NextValueSurrogate(path = value.path))
            }
            NextDepSurrogate.serializer().serialize(encoder, surrogate)
        }

        override fun deserialize(decoder: Decoder): NextDep {
            val surrogate = NextDepSurrogate.serializer().deserialize(decoder)
            return when (surrogate.type) {
                "registry" -> {
                    val version = checkNotNull(surrogate.value.version) {
                        "Expected version but null. surrogate: $surrogate"
                    }
                    NextDep.Registry(version)
                }
                "local" -> {
                    val path = checkNotNull(surrogate.value.path) {
                        "Expected path but null. surrogate: $surrogate"
                    }
                    NextDep.Local(path)
                }
                else -> {
                    throw SerializationException("Unexpected type: ${surrogate.type}")
                }
            }
        }
    }

    @Serializable
    data class NextDepSurrogate(val type: String, val value: NextValueSurrogate)

    @Serializable
    data class NextValueSurrogate(val version: String? = null, val path: String? = null)

    @Serializable
    data class Dep(val packageName: String, val version: String, val repo: Repo)
    @Serializable
    data class Repo(val contents: String, val tag: String)
    data class SpagoLibrary(val packageName: String, val version: String, val sourceRoots: MutableList<VirtualFile>) :
        SyntheticLibrary(), ItemPresentation {
        override fun getSourceRoots(): MutableCollection<VirtualFile> = sourceRoots
        override fun getPresentableText() = "Spago: $packageName:$version"
        override fun getIcon(unused: Boolean) = PSIcons.SPAGO
    }
}