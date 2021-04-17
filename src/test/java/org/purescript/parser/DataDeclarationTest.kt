package org.purescript.parser

class DataDeclarationTest: PSLanguageParserTestBase("data") {
    fun testOnlyWithType() = doTest(true, true)
}