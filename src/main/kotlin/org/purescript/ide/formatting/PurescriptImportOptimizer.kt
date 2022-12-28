package org.purescript.ide.formatting

import com.intellij.lang.ImportOptimizer
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.siblings
import org.purescript.file.PSFile
import org.purescript.psi.PSPsiFactory
import org.purescript.psi.imports.*

class PurescriptImportOptimizer : ImportOptimizer {
    override fun supports(file: PsiFile): Boolean =
        file is PSFile

    override fun processFile(file: PsiFile): Runnable {
        val psFile = file as PSFile
        val module = psFile.module ?: error("File contains no Purescript module: ${file.name} ")
        val factory = PSPsiFactory(file.project)
        val importDeclarations = mergeImportDeclarations(
            module.cache.imports.map(
                ::fromPsiElement
            )
        )
        val psiElements = mutableListOf<PsiElement>()
        val implicitImportDeclarations = importDeclarations.filter { it.implicit }
        val regularImportDeclarations = importDeclarations - implicitImportDeclarations.toSet()
        for (importDeclaration in implicitImportDeclarations.sortedWith(importDeclarationComparator)) {
            psiElements += factory.createImportDeclaration(importDeclaration)
                ?: error("Could not create import declaration: $importDeclaration")
            psiElements += factory.createNewLine()
        }
        if (implicitImportDeclarations.isNotEmpty() && regularImportDeclarations.isNotEmpty()) {
            psiElements += factory.createNewLine()
        }
        for (importDeclaration in regularImportDeclarations.sortedWith(importDeclarationComparator)) {
            psiElements += factory.createImportDeclaration(importDeclaration)
                ?: error("Could not create import declaration: $importDeclaration")
            psiElements += factory.createNewLine()
        }

        return Runnable {
            for (importDeclaration in module.cache.imports) {
                importDeclaration.delete()
            }
            val where = module.whereKeyword
            where.siblings(forward = true, withSelf = false)
                .takeWhile { it is PsiWhiteSpace }
                .forEach { it.delete() }
            val toAdd = mutableListOf<PsiElement>()
            toAdd.add(factory.createNewLine())
            if (psiElements.isNotEmpty()) {
                toAdd.add(factory.createNewLine())
                toAdd.addAll(psiElements)
            }
            if (where.nextSibling != null) {
                toAdd.add(factory.createNewLine())
            }
            for (element in toAdd.reversed()) {
                module.addAfter(element, where)
            }
        }
    }

    private val importDeclarationComparator: Comparator<in ImportDeclaration> =
        Comparator.comparing<ImportDeclaration, String> { it.moduleName }
            .thenBy { it.alias }
            .thenBy { it.hiding }

    private fun fromPsiElement(importDeclaration: PSImportDeclaration): ImportDeclaration =
        ImportDeclaration(
            importDeclaration.moduleName?.name
                ?: error("Import declaration is missing module name: ${importDeclaration.text}"),
            importDeclaration.isHiding,
            importDeclaration.importedItems.map(::fromPsiElement).toSet(),
            importDeclaration.importAlias?.name
        )

    private fun fromPsiElement(importedItem: PSImportedItem): ImportedItem =
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
}
