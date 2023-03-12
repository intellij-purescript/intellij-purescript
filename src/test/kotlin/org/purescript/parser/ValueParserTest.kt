package org.purescript.parser

class ValueParserTest: PSLanguageParserTestBase("value") {
    fun testMain() = doTest(true, true)
    fun testAkwardNames() = doTest(true, true)
}