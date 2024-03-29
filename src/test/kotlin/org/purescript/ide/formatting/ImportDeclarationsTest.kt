package org.purescript.ide.formatting

import junit.framework.TestCase

class ImportDeclarationsTest : TestCase() {

    fun `test does not change already merged import declarations`() {
        val importDeclarations = setOf(ImportDeclaration("Prelude"))
        assertEquals(
            importDeclarations,
            ImportDeclarations(importDeclarations).mergedImports
        )
    }

    fun `test merges import declarations from same module without alias`() {
        val importDeclarations = setOf(
            ImportDeclaration(
                "Prelude",
                importedItems = setOf(ImportedValue("map"))
            ),
            ImportDeclaration(
                "Prelude",
                importedItems = setOf(ImportedValue("unit"))
            )
        )
        val expected = setOf(
            ImportDeclaration(
                "Prelude",
                importedItems = setOf(
                    ImportedValue("map"),
                    ImportedValue("unit")
                )
            )
        )
        assertEquals(
            expected,
            ImportDeclarations(importDeclarations).mergedImports
        )
    }

    fun `test does not merge import declarations from same module with different alias`() {
        val importDeclarations = setOf(
            ImportDeclaration(
                "Prelude",
                importedItems = setOf(ImportedValue("map")),
                alias = "A"
            ),
            ImportDeclaration(
                "Prelude",
                importedItems = setOf(ImportedValue("unit")),
                alias = "B"
            )
        )
        assertEquals(
            importDeclarations,
            ImportDeclarations(importDeclarations).mergedImports
        )
    }

    fun `test importing everything trumps importing something`() {
        val importDeclarations = setOf(
            ImportDeclaration("Prelude"),
            ImportDeclaration(
                "Prelude",
                importedItems = setOf(ImportedValue("unit"))
            )
        )
        val expected = setOf(ImportDeclaration("Prelude"))
        assertEquals(
            expected,
            ImportDeclarations(importDeclarations).mergedImports
        )
    }

    fun `test importing everything trumps importing something, even with alias`() {
        val importDeclarations = setOf(
            ImportDeclaration("Prelude", alias = "A"),
            ImportDeclaration(
                "Prelude",
                importedItems = setOf(ImportedValue("unit")),
                alias = "A"
            )
        )
        val expected = setOf(ImportDeclaration("Prelude", alias = "A"))
        assertEquals(
            expected,
            ImportDeclarations(importDeclarations).mergedImports
        )
    }

    fun `test retains hiding import declaration`() {
        val importDeclarations =
            setOf(
                ImportDeclaration(
                    "Prelude",
                    hiding = true,
                    importedItems = setOf(ImportedClass("Eq"))
                )
            )
        assertEquals(
            importDeclarations,
            ImportDeclarations(importDeclarations).mergedImports
        )
    }

    fun `test importing multiple hiding declarations hides the intersection of their imported items`() {
        val importDeclarations = setOf(
            ImportDeclaration(
                "Prelude", hiding = true, importedItems = setOf(
                    ImportedClass("Eq"),
                    ImportedClass("Show"),
                )
            ),
            ImportDeclaration(
                "Prelude",
                hiding = true,
                importedItems = setOf(ImportedClass("Show"))
            )
        )
        val expected = setOf(
            ImportDeclaration(
                "Prelude",
                hiding = true,
                importedItems = setOf(ImportedClass("Show"))
            )
        )
        assertEquals(
            expected,
            ImportDeclarations(importDeclarations).mergedImports
        )
    }

    fun `test imports difference between hiding and regular imports`() {
        val importDeclarations = setOf(
            ImportDeclaration(
                "Prelude", hiding = true, importedItems = setOf(
                    ImportedClass("Eq"),
                    ImportedClass("Show"),
                )
            ),
            ImportDeclaration(
                "Prelude",
                importedItems = setOf(ImportedClass("Show"))
            )
        )
        val expected = setOf(
            ImportDeclaration(
                "Prelude",
                hiding = true,
                importedItems = setOf(ImportedClass("Eq"))
            )
        )
        assertEquals(
            expected,
            ImportDeclarations(importDeclarations).mergedImports
        )
    }

    fun `test imports everything if difference between hiding and regular imports is empty`() {
        val importDeclarations = setOf(
            ImportDeclaration(
                "Prelude",
                hiding = true,
                importedItems = setOf(ImportedClass("Show"))
            ),
            ImportDeclaration(
                "Prelude",
                importedItems = setOf(ImportedClass("Show"))
            )
        )
        val expected = setOf(ImportDeclaration("Prelude"))
        assertEquals(
            expected,
            ImportDeclarations(importDeclarations).mergedImports
        )
    }

    fun `test merges imported data with double dot`() {
        val importDeclarations = setOf(
            ImportDeclaration(
                "Data.Maybe",
                importedItems = setOf(ImportedData("Maybe"))
            ),
            ImportDeclaration(
                "Data.Maybe",
                importedItems = setOf(
                    ImportedData(
                        "Maybe",
                        dataMembers = setOf("Just")
                    )
                )
            ),
            ImportDeclaration(
                "Data.Maybe",
                importedItems = setOf(ImportedData("Maybe", doubleDot = true))
            ),
        )
        val expected = setOf(
            ImportDeclaration(
                "Data.Maybe",
                importedItems = setOf(ImportedData("Maybe", doubleDot = true))
            )
        )
        assertEquals(
            expected,
            ImportDeclarations(importDeclarations).mergedImports
        )
    }

    fun `test renders the two groups correctly`() {
        val importDeclarations = setOf(
            ImportDeclaration("Data.Array"),
            ImportDeclaration("Prelude"),
            ImportDeclaration(
                "Data.Maybe",
                importedItems = setOf(ImportedData("Maybe", doubleDot = true))
            )
        )
        assertEquals(
            """
                import Data.Array
                import Prelude
                
                import Data.Maybe (Maybe(..))
            """.trimIndent(),
            ImportDeclarations(importDeclarations).text
        )
    }

    fun `test sorts according to alias`() {
        val importDeclarations = setOf(
            ImportDeclaration("Alphabet", alias = "C"),
            ImportDeclaration("Alphabet", alias = "A"),
            ImportDeclaration("Alphabet", alias = "B"),
        )
        assertEquals(
            """
                import Alphabet as A
                import Alphabet as B
                import Alphabet as C
            """.trimIndent(),
            ImportDeclarations(importDeclarations).text
        )
    }
}
