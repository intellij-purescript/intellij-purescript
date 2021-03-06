package org.purescript

import com.intellij.lang.Language

class PSLanguage : Language("Purescript", "text/purescript", "text/x-purescript", "application/x-purescript") {
    companion object {
        val INSTANCE = PSLanguage()

        /**
         * These modules are built into the purescript compiler,
         * and have no corresponding source files.
         *
         * See [https://pursuit.purescript.org/builtins/docs/Prim] for details.
         */
        val BUILTIN_MODULES = setOf(
            "Prim",
            "Prim.Boolean",
            "Prim.Coerce",
            "Prim.Ordering",
            "Prim.Row",
            "Prim.RowList",
            "Prim.Symbol",
            "Prim.TypeError",
        )

        /**
         * These types are built into the purescript compiles,
         * and are always available.
         *
         * See [https://pursuit.purescript.org/builtins/docs/Prim] for details.
         */
        val BUILTIN_TYPES = setOf(
            "Int",
            "Number",
            "String",
            "Char",
            "Boolean",
            "Array",
            "Type", // TODO Type is really a kind, not a type
            "Row", // TODO Row is really a kind, not a type
        )
    }
}
