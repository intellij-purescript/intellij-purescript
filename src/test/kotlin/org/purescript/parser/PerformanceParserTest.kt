package org.purescript.parser

class PerformanceParserTest: PSLanguageParserTestBase("performance") {
    fun testStatements() = doTest(true, true)
}