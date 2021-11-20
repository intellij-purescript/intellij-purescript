package org.purescript.parser

class ForeignParserTest: PSLanguageParserTestBase("foreign") {
    fun testImportData() = doTest(true, true)
}