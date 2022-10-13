package org.purescript.parser

class StringParserTest: PSLanguageParserTestBase("string") {
    fun testTripleQuoteString() = doTest(true, true)
}