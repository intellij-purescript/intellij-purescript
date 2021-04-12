package org.purescript.file

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.*
import com.intellij.util.io.EnumeratorStringDescriptor
import org.jetbrains.annotations.NonNls

class ExportedConstructorsIndex : ScalarIndexExtension<String>() {
    override fun getName(): ID<String, Void?> = NAME

    override fun getIndexer(): DataIndexer<String, Void?, FileContent> =
        DataIndexer<String, Void?, FileContent> {
            when (val file = it.psiFile) {
                is PSFile -> {
                    val typeConstructors: Sequence<String> = file.module
                        ?.exportedNewTypeDeclarations
                        ?.map { it.newTypeConstructor.name }
                        ?.asSequence()
                        ?: emptySequence<String>()
                    val dataConstructors: Sequence<String> = file.module
                        ?.exportedDataDeclarations
                        ?.flatMap { it.dataConstructors.asSequence() }
                        ?.map { it.name }
                        ?.asSequence()
                        ?: emptySequence<String>()
                    (typeConstructors + dataConstructors)
                        .map { it to null }
                        .toMap()
                }
                else -> emptyMap()
            }
        }

    override fun getKeyDescriptor() = EnumeratorStringDescriptor.INSTANCE
    override fun getVersion() = 0
    override fun getInputFilter() =
        DefaultFileTypeSpecificInputFilter(PSFileType.INSTANCE)

    override fun dependsOnFileContent() = true

    companion object {
        @NonNls
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