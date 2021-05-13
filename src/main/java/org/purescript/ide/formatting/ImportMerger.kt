package org.purescript.ide.formatting

data class ImportDeclaration(
    val moduleName: String,
    val hiding: Boolean = false,
    val importedItems: Set<ImportedItem> = emptySet(),
    val alias: String? = null
)

sealed class ImportedItem

data class ImportedClass(val name: String) : ImportedItem()
data class ImportedKind(val name: String) : ImportedItem()
data class ImportedType(val name: String) : ImportedItem()
data class ImportedData(val name: String, val doubleDot: Boolean = false, val dataMembers: Set<String> = emptySet()) : ImportedItem()
data class ImportedValue(val name: String) : ImportedItem()
data class ImportedOperator(val name: String) : ImportedItem()

fun mergeImportDeclarations(importDeclarations: Set<ImportDeclaration>): Set<ImportDeclaration> {
    return importDeclarations.groupBy { it.moduleName to it.alias }
        .map {
            val (moduleNameAndAlias, group) = it
            val (moduleName, alias) = moduleNameAndAlias
            mergeGroup(moduleName, alias, group)
        }.toSet()
}

private fun mergeGroup(moduleName: String, alias: String?, importDeclarations: List<ImportDeclaration>):
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

    return ImportDeclaration(
        moduleName,
        importedItems = importDeclarations.flatMap { it.importedItems }.toSet(),
        alias = alias
    )
}
