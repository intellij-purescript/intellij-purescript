package org.purescript.file

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.*
import com.intellij.util.io.EnumeratorStringDescriptor
import org.purescript.module.Module
import org.purescript.module.declaration.data.DataDeclaration
import org.purescript.module.declaration.foreign.PSForeignDataDeclaration
import org.purescript.module.declaration.newtype.NewtypeDecl
import org.purescript.module.declaration.type.TypeDecl
import org.purescript.module.exports.ExportedData

/**
 * An index on what type declarations every module exports.
 * The index contains the following declarations:
 *  - [TypeDecl]
 *  - [DataDeclaration.Psi]
 *  - [NewtypeDecl]
 *  - [PSForeignDataDeclaration]
 */
class ExportedTypesIndex : ScalarIndexExtension<String>(), DataIndexer<String, Void?, FileContent> {

    override fun map(inputData: FileContent): Map<String, Void?> {
        val file = inputData.psiFile as? PSFile ?: return emptyMap()
        val module = file.module ?: return emptyMap()
        val exportList = module.exports
        return if (exportList == null) {
            declarations(module)
        } else {
            val explicitlyExported = exportList.exportedItems
                .filterIsInstance<ExportedData.Psi>()
                .associate { it.name to null }
            if (module.exportsSelf) {
                explicitlyExported + declarations(module)
            } else {
                explicitlyExported
            }
        }
    }

    private fun declarations(module: Module): Map<String, Nothing?> {
        val exportedNames = mutableSetOf<String>()
        module.cache.typeSynonymDeclarations.mapTo(exportedNames) { it.name }
        module.cache.dataDeclarations.mapTo(exportedNames) { it.name }
        module.cache.newTypeDeclarations.mapTo(exportedNames) { it.name }
        module.cache.foreignDataDeclarations.mapTo(exportedNames) { it.name }
        return exportedNames.associateWith { null }
    }

    override fun getName(): ID<String, Void?> = NAME

    override fun getIndexer() = this

    override fun getKeyDescriptor(): EnumeratorStringDescriptor =
        EnumeratorStringDescriptor.INSTANCE

    override fun getVersion() = 2

    override fun getInputFilter() =
        DefaultFileTypeSpecificInputFilter(PSFileType)

    override fun dependsOnFileContent() = true

    companion object {
        val NAME = ID.create<String, Void?>("org.purescript.file.ExportedTypesIndex")
        fun filesExportingType(project: Project, typeName: String): List<PSFile> =
            ReadAction.compute<List<PSFile>, Throwable> {
                val allScope = GlobalSearchScope.allScope(project)
                val index = FileBasedIndex.getInstance()
                val files = index.getContainingFiles(NAME, typeName, allScope)
                files
                    .map { PsiManager.getInstance(project).findFile(it) }
                    .filterIsInstance(PSFile::class.java)
            }
    }

}
