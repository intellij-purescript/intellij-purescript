package org.purescript.file

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.*
import com.intellij.util.io.EnumeratorStringDescriptor
import org.purescript.module.exports.ExportedData
import org.purescript.module.exports.ExportedType

class ExportedConstructorsIndex : ScalarIndexExtension<String>() {

    override fun getName(): ID<String, Void?> = NAME

    override fun getIndexer(): DataIndexer<String, Void?, FileContent> =
        DataIndexer<String, Void?, FileContent> {
            when (val file = it.psiFile) {
                is PSFile -> {
                    when {
                        // failed parsing file
                        file.module == null -> emptyMap()
                        // exports all
                        file.module?.exports == null -> {
                            val dataConstructors = file.module
                                ?.cache
                                ?.dataConstructors
                                ?.map { it.name }
                                ?.asSequence()
                                ?: emptySequence()
                            val typeConstructors = file.module
                                ?.cache
                                ?.newTypeConstructors
                                ?.map { it.name }
                                ?.asSequence()
                                ?: emptySequence()
                            (typeConstructors + dataConstructors)
                                .map { it to null }
                                .toMap()
                        }
                        // exports named in the export list
                        else -> {
                            file.module?.exports!!.exportedItems
                                .mapNotNull {
                                    when (it) {
                                        is ExportedData.Psi -> it.dataDeclaration
                                            ?.dataConstructors
                                            ?.map { it.name }
                                            ?.asSequence()

                                        is ExportedType.Psi -> sequenceOf(it.name)
                                        else -> null
                                    }
                                }
                                .flatMap { it }
                                .associateWith { null }
                        }
                    }
                }
                else -> emptyMap()
            }
        }

    override fun getKeyDescriptor(): EnumeratorStringDescriptor =
        EnumeratorStringDescriptor.INSTANCE

    override fun getVersion() = 1

    override fun getInputFilter() =
        DefaultFileTypeSpecificInputFilter(PSFileType)

    override fun dependsOnFileContent() = true

    companion object {
        val NAME =
            ID.create<String, Void?>("org.purescript.file.ExportedConstructorsIndex")

        fun filesExportingConstructor(
            project: Project,
            constructor: String
        ): List<PSFile> =
            ReadAction.compute<List<PSFile>, Throwable> {
                val allScope = GlobalSearchScope.allScope(project)
                val files = FileBasedIndex
                    .getInstance()
                    .getContainingFiles(NAME, constructor, allScope)
                files
                    .map { PsiManager.getInstance(project).findFile(it) }
                    .filterIsInstance(PSFile::class.java)
            }
    }

}
