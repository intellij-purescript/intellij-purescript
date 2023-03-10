package org.purescript.run.spago

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.AdditionalLibraryRootsProvider
import com.intellij.openapi.vfs.VirtualFile

class SpagoLibraryRootsProvider : AdditionalLibraryRootsProvider() {
    override fun getAdditionalProjectLibraries(project: Project) = project.service<Spago>().libraries
    override fun getRootsToWatch(project: Project): MutableCollection<VirtualFile> {
        return getAdditionalProjectLibraries(project)
            .filter { it.version == "local" }
            .flatMap { it.sourceRoots }
            .toMutableList()
    }
}