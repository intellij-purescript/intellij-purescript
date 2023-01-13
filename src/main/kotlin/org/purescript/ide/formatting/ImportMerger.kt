package org.purescript.ide.formatting

data class ImportDeclaration(
    val moduleName: String,
    val hiding: Boolean = false,
    val importedItems: Set<ImportedItem> = emptySet(),
    val alias: String? = null
) {
    val implicit = alias == null && (hiding || importedItems.isEmpty())
}

sealed class ImportedItem(open val name: String)

data class ImportedClass(override val name: String) : ImportedItem(name)
data class ImportedType(override val name: String) : ImportedItem(name)
data class ImportedValue(override val name: String) : ImportedItem(name)
data class ImportedOperator(override val name: String) : ImportedItem(name)
data class ImportedData(
    override val name: String,
    val doubleDot: Boolean = false,
    val dataMembers: Set<String> = emptySet()
) : ImportedItem(name)

fun mergeImportDeclarations(importDeclarations: Iterable<ImportDeclaration>): Set<ImportDeclaration> {
    return importDeclarations.toSet()
        .groupBy { it.moduleName to it.alias }
        .map {(moduleNameAndAlias, group) ->
            val (moduleName, alias) = moduleNameAndAlias
            mergeGroup(moduleName, alias, group)
        }.toSet()
}

private fun mergeGroup(
    moduleName: String,
    alias: String?,
    importDeclarations: List<ImportDeclaration>
):
    ImportDeclaration {
    if (importDeclarations.any { it.importedItems.isEmpty() }) {
        return ImportDeclaration(moduleName, alias = alias)
    }

    val anyHiding = importDeclarations.any { it.hiding }
    if (anyHiding) {
        val hiddenItems = importDeclarations
            .filter { it.hiding }
            .map { it.importedItems }
            .reduce { acc, importedItems -> acc.intersect(importedItems) }
        val importedItems = importDeclarations
            .filterNot { it.hiding }
            .flatMap { it.importedItems }
            .toSet()
        val difference = hiddenItems - importedItems
        return ImportDeclaration(
            moduleName,
            hiding = difference.isNotEmpty(),
            importedItems = difference,
            alias = alias
        )
    }

    val importedItems = importDeclarations
        .flatMap { it.importedItems }
        .let { mergeImportedItems(it) }

    return ImportDeclaration(
        moduleName,
        importedItems = importedItems,
        alias = alias
    )
}

fun mergeImportedItems(importedItems: Iterable<ImportedItem>): Set<ImportedItem> {
    val mergedImportItems = mutableSetOf<ImportedItem>()
    importedItems.filterTo(mergedImportItems) { it !is ImportedData }
    importedItems
        .filterIsInstance<ImportedData>()
        .groupBy { it.name }
        .mapTo(mergedImportItems) {
            val (name, importedDataItems) = it
            if (importedDataItems.any { it.doubleDot }) {
                ImportedData(name, doubleDot = true)
            } else {
                val dataMembers =
                    importedDataItems.flatMap { it.dataMembers }.toSet()
                ImportedData(name, dataMembers = dataMembers)
            }
        }
    return mergedImportItems
}
