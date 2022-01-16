package org.purescript.file

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.*
import com.intellij.util.io.EnumeratorStringDescriptor
import org.purescript.psi.exports.PSExportedData
import org.purescript.psi.exports.PSExportedType

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
                        file.module?.exportList == null -> {
                            val dataConstructors = file.module
                                ?.dataConstructors
                                ?.map { it.name }
                                ?.asSequence()
                                ?: emptySequence()
                            val typeConstructors = file.module
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
                            file.module!!.exportList!!.exportedItems
                                .mapNotNull { when(it) {
                                    is PSExportedData -> it.dataDeclaration
                                        ?.dataConstructors
                                        ?.map {it.name }
                                        ?.asSequence()
                                    is PSExportedType -> sequenceOf(it.name)
                                    else -> null
                                } }
                                .flatMap { it }
                                .map { it to null }
                                .toMap()
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
        DefaultFileTypeSpecificInputFilter(PS_FILE_TYPE_INSTANCE)

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
