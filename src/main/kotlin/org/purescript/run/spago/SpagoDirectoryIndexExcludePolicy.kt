package org.purescript.run.spago

import com.intellij.openapi.project.guessModuleDir
import com.intellij.openapi.roots.ModuleRootModel
import com.intellij.openapi.roots.impl.DirectoryIndexExcludePolicy
import com.intellij.openapi.vfs.impl.LightFilePointer
import com.intellij.openapi.vfs.pointers.VirtualFilePointer

class SpagoDirectoryIndexExcludePolicy: DirectoryIndexExcludePolicy {
    override fun getExcludeRootsForModule(rootModel: ModuleRootModel): Array<VirtualFilePointer> {
        return rootModel.module.guessModuleDir()?.findChild(".spago")?.let{ arrayOf(LightFilePointer(it)) }
            ?: emptyArray()
    }
}