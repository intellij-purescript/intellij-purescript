package org.purescript.parser

class NewtypeParserTest: PSLanguageParserTestBase("newtype") {
    fun testNested() = doTest(true, true)
}