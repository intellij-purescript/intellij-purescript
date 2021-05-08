package org.purescript.file

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.*
import com.intellij.util.io.EnumeratorStringDescriptor
import org.purescript.psi.PSForeignDataDeclaration
import org.purescript.psi.data.PSDataDeclaration
import org.purescript.psi.newtype.PSNewTypeDeclarationImpl
import org.purescript.psi.typesynonym.PSTypeSynonymDeclaration

/**
 * An index on what type declarations every module exports.
 * The index contains the following declarations:
 *  - [PSTypeSynonymDeclaration]
 *  - [PSDataDeclaration]
 *  - [PSNewTypeDeclarationImpl]
 *  - [PSForeignDataDeclaration]
 */
class ExportedTypesIndex : ScalarIndexExtension<String>(), DataIndexer<String, Void?, FileContent> {

    override fun map(inputData: FileContent): Map<String, Void?> {
        val file = inputData.psiFile as? PSFile ?: return emptyMap()
        val module = file.module ?: return emptyMap()
        val exportedNames = mutableSetOf<String>()
        module.exportedTypeSynonymDeclarations.mapTo(exportedNames) { it.name }
        module.exportedDataDeclarations.mapTo(exportedNames) { it.name }
        module.exportedNewTypeDeclarations.mapTo(exportedNames) { it.name }
        module.exportedForeignDataDeclarations.mapTo(exportedNames) { it.name }
        return exportedNames.associateWith { null }
    }

    override fun getName(): ID<String, Void?> = NAME

    override fun getIndexer() = this

    override fun getKeyDescriptor(): EnumeratorStringDescriptor =
        EnumeratorStringDescriptor.INSTANCE

    override fun getVersion() = 1

    override fun getInputFilter() =
        DefaultFileTypeSpecificInputFilter(PSFileType.INSTANCE)

    override fun dependsOnFileContent() = true

    companion object {
        val NAME =
            ID.create<String, Void?>("org.purescript.file.ExportedTypesIndex")

        fun filesExportingType(
            project: Project,
            typeName: String
        ): List<PSFile> =
            ReadAction.compute<List<PSFile>, Throwable> {
                val allScope = GlobalSearchScope.allScope(project)
                val files = FileBasedIndex
                    .getInstance()
                    .getContainingFiles(NAME, typeName, allScope)
                files
                    .map { PsiManager.getInstance(project).findFile(it) }
                    .filterIsInstance(PSFile::class.java)
            }
    }

}
