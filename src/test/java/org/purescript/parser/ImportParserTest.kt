package org.purescript.parser

class ImportParserTest: PSLanguageParserTestBase("import") {
    fun testMultipleImports() = doTest(true, true)
}