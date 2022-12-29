package org.purescript

import com.intellij.lang.Language

object PSLanguage : Language("Purescript", "text/purescript", "text/x-purescript", "application/x-purescript") {
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
            "Prim.Int",
            "Prim.TypeError",
        )

        /**
         * These types are built into the purescript compiles,
         * and are always available.
         *
         * See [https://pursuit.purescript.org/builtins/docs/Prim] for details.
         */
        val BUILTIN_TYPES = setOf(
            "Array",
            "Boolean",
            "Char",
            "Constraint",
            "Function", // TODO Function is really a kind, not a type
            "Int",
            "Number",
            "Partial", // TODO Partial is really a Class, not a type
            "Record", // TODO Record is really a kind, not a type
            "Row", // TODO Row is really a kind, not a type
            "String",
            "Symbol", 
            "Type", // TODO Type is really a kind, not a type
            // These should be imported from respective Prim modules, but quick fix is to always allow them
            // Prim.Boolean
            "True",
            "False",
            // Prim.Coerce
            "Coercible",
            // Prim.Ordering
            "Ordering",
            "LT",
            "EQ",
            "GT",
            // Prim.Row
            "Union",
            "Nub",
            "Lacks",
            "Cons",
            // Prim.RowList
            "RowList",
            "Cons",
            "Nil",
            "RowToList",
            // Prim.Symbol
            "Append",
            "Compare",
            "Cons",
            // Prim.Int
            "Add",
            "Compare",
            "Mul",
            "ToString",
            // Prim.TypeError
            "Warn",
            "Fail",
            "Doc",
            "Text",
            "Quote",
            "QuoteLabel",
            "Beside",
            "Above",
        )
}
