package org.purescript.file

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.*
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import org.jetbrains.annotations.NonNls

class ImportedModuleIndex : ScalarIndexExtension<String>() {
    override fun getName(): ID<String, Void?> {
        return NAME
    }

    override fun getIndexer(): DataIndexer<String, Void?, FileContent> {
        return DataIndexer<String, Void?, FileContent> {
            when (val file = it.psiFile) {
                is PSFile -> file.module?.cache?.imports
                    ?.map { it.moduleName.name }
                    ?.associateWith { null }
                    ?: emptyMap()
                else -> emptyMap()
            }
        }
    }

    override fun getKeyDescriptor(): KeyDescriptor<String> =
        EnumeratorStringDescriptor.INSTANCE

    override fun getVersion(): Int = 0

    override fun getInputFilter(): FileBasedIndex.InputFilter =
        DefaultFileTypeSpecificInputFilter(PSFileType.INSTANCE)

    override fun dependsOnFileContent(): Boolean = true

    companion object {
        @NonNls
        val NAME =
            ID.create<String, Void?>("org.purescript.file.ImportedModuleIndex")

        fun filesImportingModule(
            project: Project,
            moduleName: String
        ): MutableCollection<VirtualFile> {
            val fileBasedIndex = FileBasedIndex.getInstance()
            return ReadAction.compute<MutableCollection<VirtualFile>, Throwable> {
                fileBasedIndex.getContainingFiles(
                    NAME,
                    moduleName,
                    GlobalSearchScope.allScope(project)
                )
            }
        }
    }

}