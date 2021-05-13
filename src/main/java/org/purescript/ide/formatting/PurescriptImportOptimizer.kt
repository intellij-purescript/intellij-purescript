package org.purescript.ide.formatting

import com.intellij.lang.ImportOptimizer
import com.intellij.psi.PsiFile
import org.purescript.file.PSFile
import org.purescript.psi.PSPsiFactory
import org.purescript.psi.imports.*

class PurescriptImportOptimizer : ImportOptimizer {
    override fun supports(file: PsiFile): Boolean =
        file is PSFile

    override fun processFile(file: PsiFile): Runnable {
        val psFile = file as PSFile
        val module = psFile.module ?: error("File ${file.name} contains no Purescript module")
        val factory = PSPsiFactory(file.project)
        val importDeclarations = module.importDeclarations.map { importDeclaration ->
            ImportDeclaration(
                importDeclaration.moduleName!!.name,
                importDeclaration.isHiding,
                importDeclaration.importedItems.map { importedItem ->
                    when (importedItem) {
                        is PSImportedClass -> ImportedClass(importedItem.name)
                        is PSImportedKind -> ImportedKind(importedItem.name)
                        is PSImportedType -> ImportedType(importedItem.name)
                        is PSImportedData -> ImportedData(
                            importedItem.name,
                            importedItem.importsAll,
                            importedItem.importedDataMembers.map { it.name }.toSet()
                        )
                        is PSImportedValue -> ImportedValue(importedItem.name)
                        is PSImportedOperator -> ImportedOperator(importedItem.name)
                    }
                }.toSet(),
                alias = importDeclaration.importAlias?.name
            )
        }.toSet()

        val mergedImportDeclarations = mergeImportDeclarations(importDeclarations)
            .map { factory.createImportDeclaration(it)!! }
        return Runnable { module.replaceImportDeclarations(mergedImportDeclarations) }
    }
}
