package org.purescript.parser

class AdoParserTest: PSLanguageParserTestBase("ado") {
    fun testPureSugar() = doTest(true, true)
    fun testMapSugar() = doTest(true, true)
    fun testInWithRecord() = doTest(true, true)
    fun testWithPrefix() = doTest(true, true)
}