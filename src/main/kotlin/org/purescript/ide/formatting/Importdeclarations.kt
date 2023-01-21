package org.purescript.ide.formatting

data class ImportDeclarations(val imports: Set<ImportDeclaration>) {
    val mergedImports
        get() = imports
            .groupBy { it.moduleName to it.alias }
            .map { (moduleNameAndAlias, group) ->
                val (moduleName, alias) = moduleNameAndAlias
                mergeGroup(moduleName, alias, group)
            }.toSet()
    val text
        get() : String = buildString {
            val imports = mergedImports
                .sortedBy { it.moduleName }
                .sortedBy { it.alias }
                .sortedBy { it.hiding }
            val implicit = imports.filter { it.implicit }
            if (implicit.isNotEmpty()) {
                append(implicit.joinToString("\n") { it.toString() })
            }
            val explicit = imports.filter { it.explicit }
            if (implicit.isNotEmpty() && explicit.isNotEmpty()) {
                appendLine()
                appendLine()
            }
            if (explicit.isNotEmpty()) {
                append(explicit.joinToString("\n") { it.toString() })
            }
        }

    companion object {
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
    }
}

data class ImportDeclaration(
    val moduleName: String,
    val hiding: Boolean = false,
    val importedItems: Set<ImportedItem> = emptySet(),
    val alias: String? = null
) {
    fun withAlias(alias: String?) =
        ImportDeclaration(moduleName, hiding, importedItems, alias)

    /**
     * Implicit is for example
     * @code {
     * import Prelude
     * }
     */
    val implicit = alias == null && (hiding || importedItems.isEmpty())

    /**
     * Explicit is for example
     * @code {
     * import Prelude ((+))
     * }
     * where (+) is explicitly imported
     */
    val explicit = !implicit
    private val sortedItems: List<ImportedItem>
        get() = importedItems.sortedBy { it.name }.sortedBy { sortKey(it) }

    override fun toString() = buildString {
        append("import $moduleName")
        if (importedItems.isNotEmpty()) {
            if (hiding) append(" hiding")
            append(" (${sortedItems.joinToString { it.text }})")
        }
        if (alias != null) {
            append(" as $alias")
        }
    }

    fun withItems(vararg items: ImportedItem) = copy(importedItems=items.toSet())

    companion object {
        private fun sortKey(it: ImportedItem) = when (it) {
            is ImportedClass -> 1
            is ImportedType -> 3
            is ImportedData -> 4
            is ImportedValue -> 5
            is ImportedOperator -> 6
        }
    }
}

sealed class ImportedItem(open val name: String) {
    abstract val text: String
}

data class ImportedClass(override val name: String) : ImportedItem(name) {
    override val text get() = "class $name"
}

data class ImportedType(override val name: String) : ImportedItem(name) {
    override val text get() = "type ($name)"
}

data class ImportedValue(override val name: String) : ImportedItem(name) {
    override val text get() = name
}

data class ImportedOperator(override val name: String) : ImportedItem(name) {
    override val text get() = "($name)"
}

data class ImportedData(
    override val name: String,
    val doubleDot: Boolean = false,
    val dataMembers: Set<String> = emptySet()
) : ImportedItem(name) {
    override val text
        get() = when {
            doubleDot -> "${name}(..)"
            dataMembers.isEmpty() -> name
            else -> "${name}(${dataMembers.sorted().joinToString()})"
        }
}

