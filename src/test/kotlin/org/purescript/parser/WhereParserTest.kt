package org.purescript.parser

class WhereParserTest: PSLanguageParserTestBase("where") {
    fun testOneLine() = doTest(true, true)
    fun testNewLine() = doTest(true, true)
    fun testTwoLines() = doTest(true, true)
    fun testCase() = doTest(true, true)
    fun testCaseIndent() = doTest(true, true)
    fun testCaseDedent() = doTest(true, true)
}