package org.purescript.graph

import org.purescript.ide.formatting.ImportedItem

sealed interface Filter {
    fun match(item: ImportedItem): Boolean
    object Any: Filter {
        override fun match(item: ImportedItem): Boolean = true
    }

    data class Hiding(val items: Set<ImportedItem>):Filter {
        override fun match(item: ImportedItem): Boolean = items.none { it.includes(item) }
    }

    data class Explicit(val items: Set<ImportedItem>):Filter {
        override fun match(item: ImportedItem): Boolean = items.any { it.includes(item) }
    }
    data class Compose(val filters: List<Filter>): Filter {
        override fun match(item: ImportedItem): Boolean = filters.any { it.match(item) }
    }
}