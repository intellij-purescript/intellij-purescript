package org.purescript.parser

class RowAndRecordParserTest : PSLanguageParserTestBase("row-and-record") {
    fun testRowConstructors() = doTest(true, true)
    fun testRowInInstanceHeadDetermined() = doTest(true, true)
    fun testRowPolyInstanceContext() = doTest(true, true)
    fun testRowsInInstanceContext() = doTest(true, true)
    fun testRowUnion() = doTest(true, true)
    fun testNestedRecordUpdate() = doTest(true, true)
    fun testNestedRecordUpdateWildcards() = doTest(true, true)
}