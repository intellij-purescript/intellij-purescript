package org.purescript.lexer

class RowAndRecordLexerTest: PSLexerTestBase("row-and-record") {
    fun testRowConstructors() = doFileTest("purs")
    fun testRowConstructorsWithWildcards() = doFileTest("purs")
    fun testRowInInstanceHeadDetermined() = doFileTest("purs")
    fun testRowPolyInstanceContext() = doFileTest("purs")
    fun testRowsInInstanceContext() = doFileTest("purs")
    fun testRowUnion() = doFileTest("purs")
    fun testRecordWithStringAsLabel() = doFileTest("purs")
    fun testNestedRecordUpdate() = doFileTest("purs")
    fun testNestedRecordUpdateWildcards() = doFileTest("purs")
}