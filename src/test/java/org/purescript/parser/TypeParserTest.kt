package org.purescript.parser

class TypeParserTest: PSLanguageParserTestBase("type") {
    fun testPolyKinds() = doTest(true, true)
    fun testPolyNewKinds() = doTest(true, true)
}