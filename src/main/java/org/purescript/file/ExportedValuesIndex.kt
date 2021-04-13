package org.purescript.file

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.*
import com.intellij.util.io.EnumeratorStringDescriptor
import org.jetbrains.annotations.NonNls
import org.purescript.psi.exports.PSExportedValue

class ExportedValuesIndex : ScalarIndexExtension<String>() {
    override fun getName(): ID<String, Void?> = NAME

    override fun getIndexer(): DataIndexer<String, Void?, FileContent> =
        DataIndexer<String, Void?, FileContent> {
            when (val file = it.psiFile) {
                is PSFile -> {
                    when {
                        // failed parsing file
                        file.module == null -> emptyMap()
                        // exports all
                        file.module?.exportList == null -> {
                            val exportedValues = file.module!!
                                .valueDeclarations
                                .map { it.name }
                                .toSet()
                                .asSequence()
                            val exportedForeignValues = file.module!!
                                .exportedForeignValueDeclarations
                                .mapNotNull { it.name }
                                .asSequence()
                            (exportedValues + exportedForeignValues)
                                .map { it to null }
                                .toMap()
                        }
                        else -> {
                            file.module!!.exportList!!.exportedItems
                                .mapNotNull { when(it) {
                                    is PSExportedValue -> it.name
                                    else -> null
                                } }
                                .map { it to null }
                                .toMap()
                        }
                    }
                }
                else -> emptyMap()
            }
        }

    override fun getKeyDescriptor() = EnumeratorStringDescriptor.INSTANCE
    override fun getVersion() = 1
    override fun getInputFilter() =
        DefaultFileTypeSpecificInputFilter(PSFileType.INSTANCE)

    override fun dependsOnFileContent() = true

    companion object {
        @NonNls
        val NAME =
            ID.create<String, Void?>("org.purescript.file.ExportedValuesIndex")

        fun filesExportingValue(
            project: Project,
            value: String
        ): List<PSFile> =
            ReadAction.compute<List<PSFile>, Throwable> {
                val allScope = GlobalSearchScope.allScope(project)
                val files = FileBasedIndex
                    .getInstance()
                    .getContainingFiles(NAME, value, allScope)
                files
                    .map { PsiManager.getInstance(project).findFile(it) }
                    .filterIsInstance(PSFile::class.java)
            }
    }

}