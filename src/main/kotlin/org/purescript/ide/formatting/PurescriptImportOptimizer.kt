package org.purescript.ide.formatting

import com.intellij.lang.ImportOptimizer
import com.intellij.openapi.components.service
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.siblings
import org.purescript.file.PSFile
import org.purescript.psi.PSPsiFactory
import org.purescript.psi.imports.*

class PurescriptImportOptimizer : ImportOptimizer {
    override fun supports(file: PsiFile): Boolean = file is PSFile.Psi
    override fun processFile(file: PsiFile): Runnable {
        val psFile = file as PSFile.Psi
        val module = psFile.module
            ?: error("File contains no Purescript module: ${file.name} ")
        val factory: PSPsiFactory = file.project.service()
        val importDeclarationsFromModule = 
            module.cache.imports.map(::fromPsiElement)
        val importDeclarations = 
            ImportDeclarations(importDeclarationsFromModule.toSet())
        val mergedImports = importDeclarations.mergedImports
        val implicitImportDeclarations =
            mergedImports.filter { it.implicit }
        val regularImportDeclarations =
            mergedImports - implicitImportDeclarations.toSet()

        val implicitPsi = if (implicitImportDeclarations.isEmpty()) {
            null
        } else factory.createImportDeclarations(
            implicitImportDeclarations.sortedWith(
                importDeclarationComparator
            )
        );

        val regularPsi = if (regularImportDeclarations.isEmpty()) null 
        else factory.createImportDeclarations(regularImportDeclarations.sortedWith(importDeclarationComparator))
        return Runnable {
            for (importDeclaration in module.cache.imports) {
                importDeclaration.delete()
            }
            val where = module.whereKeyword
            where.siblings(forward = true, withSelf = false)
                .takeWhile { it is PsiWhiteSpace }
                .forEach { it.delete() }
            if (where.nextSibling != null) {
                module.addAfter(factory.createNewLines(2), where)
            } else {
                module.addAfter(factory.createNewLines(1), where)
            }
            when (regularPsi) {
                null , null to null -> {}
                else -> {
                    module.addRangeAfter(
                        regularPsi.first,
                        regularPsi.second,
                        where
                    )
                    module.addAfter(factory.createNewLines(2), where)
                }
            }
            when (implicitPsi) {
                null , null to null -> {}
                else -> {
                    module.addRangeAfter(
                        implicitPsi.first,
                        implicitPsi.second,
                        where
                    )
                    module.addAfter(factory.createNewLines(2), where)
                }
            }
        }
    }

    private val importDeclarationComparator: Comparator<in ImportDeclaration> =
        Comparator.comparing<ImportDeclaration, String> { it.moduleName }
            .thenBy { it.alias }
            .thenBy { it.hiding }

    private fun fromPsiElement(importDeclaration: Import.Psi): ImportDeclaration =
        ImportDeclaration(
            importDeclaration.moduleName.name,
            importDeclaration.isHiding,
            importDeclaration.importedItems.map(::fromPsiElement).toSet(),
            importDeclaration.importAlias?.name
        )

    private fun fromPsiElement(importedItem: PSImportedItem): ImportedItem =
        when (importedItem) {
            is PSImportedClass -> ImportedClass(importedItem.name)
            is PSImportedType -> ImportedType(importedItem.name)
            is PSImportedData -> ImportedData(
                importedItem.name,
                importedItem.importsAll,
                importedItem.importedDataMembers.map { it.name }.toSet()
            )

            is PSImportedValue -> ImportedValue(importedItem.name)
            is PSImportedOperator -> ImportedOperator(importedItem.name)
        }
}
