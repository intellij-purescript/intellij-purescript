package org.purescript.parser

class DoParserTest: PSLanguageParserTestBase("do") {
    fun testQualifiedDo() = doTest(true, true)
}