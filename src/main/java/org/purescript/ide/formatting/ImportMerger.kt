package org.purescript.ide.formatting

data class ImportDeclaration(
    val moduleName: String,
    val hiding: Boolean = false,
    val importedItems: Set<ImportedItem> = emptySet(),
    val alias: String? = null
) {
    val implicit = hiding || importedItems.isEmpty()
}

sealed class ImportedItem(open val name: String)

data class ImportedClass(override val name: String) : ImportedItem(name)
data class ImportedKind(override val name: String) : ImportedItem(name)
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
