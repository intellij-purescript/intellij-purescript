package org.purescript.ide.formatting

import com.intellij.lang.ImportOptimizer
import com.intellij.psi.PsiFile
import org.purescript.file.PSFile
import org.purescript.psi.PSPsiFactory
import org.purescript.psi.imports.PSImportDeclarationImpl

class PurescriptImportOptimizer : ImportOptimizer {
    override fun supports(file: PsiFile): Boolean =
        file is PSFile

    override fun processFile(file: PsiFile): Runnable {
        val psFile = file as? PSFile ?: return NOP
        val module = psFile.module ?: return NOP
        val currentImportDeclarations = module.importDeclarations
        if (currentImportDeclarations.isEmpty()) {
            return NOP
        }
        val groupedCurrentImportDeclarations = currentImportDeclarations.groupBy {
            val moduleName = it.moduleName?.name
            val hiding = it.isHiding
            val alias = it.importAlias?.name
            Triple(moduleName, hiding, alias)
        }
        val factory = PSPsiFactory(file.project)
        val newImportDeclarations = mutableListOf<PSImportDeclarationImpl>()
        for ((key, importDeclarations) in groupedCurrentImportDeclarations) {
            val (moduleName, hiding, alias) = key
            if (moduleName == null) {
                continue
            }
            val importedDataElements = if (importDeclarations.any { it.importList == null }) {
                emptyList()
            }
            else {
                importDeclarations.mapNotNull { it.importList }
                    .flatMap { it.importedItems.toList() }
                    .distinctBy { it.text }
                    .sorted()
            }
            val mergedImportDeclaration = factory.createImportDeclaration(moduleName, hiding, importedDataElements, alias)
                ?: return NOP
            newImportDeclarations += mergedImportDeclaration
        }
        return Runnable { module.replaceImportDeclarations(newImportDeclarations.sorted()) }
    }

    companion object {
        val NOP = Runnable {}
    }
}
