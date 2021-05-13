package org.purescript.ide.formatting

import com.intellij.lang.ImportOptimizer
import com.intellij.psi.PsiFile
import com.jetbrains.rd.util.addUnique
import org.purescript.file.PSFile
import org.purescript.psi.PSPsiFactory
import org.purescript.psi.imports.*

class PurescriptImportOptimizer : ImportOptimizer {
    override fun supports(file: PsiFile): Boolean =
        file is PSFile

    override fun processFile(file: PsiFile): Runnable {
        val psFile = file as PSFile
        val module = psFile.module ?: error("File ${file.name} contains no Purescript module")
        val currentImportDeclarations = module.importDeclarations
        val factory = PSPsiFactory(file.project)
        val newImportDeclarations = mutableListOf<PSImportDeclarationImpl>()

        // We want to merge all the import declarations that share module and alias
        val importDeclarationsByModuleAndAlias = currentImportDeclarations
            .groupBy { it.moduleName!!.name to it.importAlias?.name }
        for ((moduleAndAlias, importDeclarations) in importDeclarationsByModuleAndAlias) {
            val (moduleName, alias) = moduleAndAlias

            // If any import declaration imports everything, we can just add that
            // declaration and be done
            if (importDeclarations.any { it.importsAll }) {
                newImportDeclarations += factory.createImportDeclaration(moduleName, alias = alias)!!
                continue
            }

            val hiddenItems = importDeclarations.filter { it.isHiding }
                .mapNotNull { it.importList }
                .map { it.importedItems.toSet() }
                .reduceOrNull { acc, importedItems -> acc.intersect(importedItems) }

            // If hiddenItems is null, we do not have any hiding declarations
            if (hiddenItems == null) {
                val mergedImportedItems = mutableListOf<PSImportedItem>()
                val allImportedItems = importDeclarations.flatMap { it.importedItems.toList() }

                allImportedItems.filterIsInstance<PSImportedData>()
                    .groupBy { it.name }
                    .map { importedDataByName ->
                        val (name, importedDataList) = importedDataByName
                    }

                // Imported items that are not data
                allImportedItems.filter {it !is PSImportedData}
                    .groupBy { it.javaClass }
                    .flatMapTo(mergedImportedItems) { importedItemsByClass -> importedItemsByClass.value.distinctBy { it.name } }



                val importedItemsByClass = importDeclarations.flatMap { it.importedItems.toList() }
                    .groupBy { it.javaClass }

                for ((clazz, importedItems) in importedItemsByClass) {
                    when (clazz) {
                        is PSImportedData -> {
                        }
                    }
                }

                // All imported items that are not data can go directly into the list
                allImportedItems
                    .groupBy { it.javaClass }
                    .filterNotTo(mergedImportedItems) { it is PSImportedData }

                // If there are multiple imported data that import different constructors,
                // we need to merge them
                val groupedImportedData = allImportedItems.filterIsInstance<PSImportedData>().groupBy { it.name }
                for ((name, importedDataElements) in groupedImportedData) {
                    val importsAll = importedDataElements.any { it.importsAll }
                    if (importsAll) {
                        mergedImportedItems += factory.createImportedData(name, doubleDot = true)
                            ?: return DoNothing
                        continue
                    }
                }
                // We don't have any hiding declarations, nor any declarations
                // importing everything, so the imported items are just the union
                // of all the declarations
                val importedItems = allImportedItems
                    .distinct()
                    .sorted()

                val importDeclaration = factory.createImportDeclaration(
                    moduleName,
                    importedItems = importedItems,
                    alias = alias
                ) ?: return DoNothing

                newImportDeclarations += importDeclaration
            } else {
                // If we have hiding import declarations, but the intersection of all the
                // hidden items is empty, we are in fact importing everything
                if (hiddenItems.isEmpty()) {
                    newImportDeclarations += factory.createImportDeclaration(moduleName, alias = alias)!!
                    continue
                }

                val importedItems = importDeclarations.filterNot { it.isHiding }
                    .flatMap { it.importedItems.toList() }
                    .toSet()

                // We keep the hiding import declaration, but remove any element that is imported
                // by another declaration
                val newHiddenItems = hiddenItems.subtract(importedItems)
                    .toList()
                    .sorted()
                val importDeclaration = factory.createImportDeclaration(
                    moduleName,
                    hiding = true,
                    importedItems = newHiddenItems,
                    alias = alias
                ) ?: return DoNothing

                newImportDeclarations += importDeclaration
            }
        }
        return Runnable { module.replaceImportDeclarations(newImportDeclarations.sorted()) }
    }

    companion object {
        val DoNothing = Runnable {}
    }
}
