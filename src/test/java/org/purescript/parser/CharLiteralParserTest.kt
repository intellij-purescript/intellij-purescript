package org.purescript.parser

class CharLiteralParserTest: PSLanguageParserTestBase("char-literal") {
    fun testTopLevel() = doTest(true, true)
    fun testTopLevelBinder() = doTest(true, true)
}