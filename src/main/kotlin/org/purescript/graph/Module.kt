package org.purescript.graph

import org.purescript.ide.formatting.ImportDeclaration

data class Module(val name: String, val exportedImports: List<ImportDeclaration>) {
    val filter: Filter
        get() {
            return Filter.Compose(
                exportedImports.map {
                    when {
                        it.hiding -> Filter.Hiding(it.importedItems)
                        it.explicit -> Filter.Explicit(it.importedItems)
                        else -> Filter.Any
                    }
                }
            )
        }
}