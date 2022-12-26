package org.purescript.file

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.*
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import org.jetbrains.annotations.NonNls

class ModuleNameIndex : ScalarIndexExtension<String>() {
    override fun getName(): ID<String, Void?> {
        return NAME
    }

    override fun getIndexer(): DataIndexer<String, Void?, FileContent> {
        return DataIndexer<String, Void?, FileContent> {
            when (val file = it.psiFile) {
                is PSFile -> file.module?.name?.let { mapOf(it to null) }
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
            ID.create<String, Void?>("org.purescript.file.ModuleNameIndex")

        fun fileContainingModule(
            project: Project,
            moduleName: String
        ): PSFile? {
            val fileBasedIndex = FileBasedIndex.getInstance()
            return ReadAction.compute<PSFile?, Throwable> {
                val files = fileBasedIndex.getContainingFiles(
                    NAME,
                    moduleName,
                    GlobalSearchScope.allScope(project)
                )
                files
                    //.filter { it.isValid }
                    .map { PsiManager.getInstance(project).findFile(it) }
                    .filterIsInstance(PSFile::class.java)
                    .firstOrNull()
            }
        }

        fun getModuleNameFromFile(project: Project,file: VirtualFile) =
            FileBasedIndex
                .getInstance()
                .getFileData(NAME, file, project)
                .keys
                .firstOrNull()
    }

}