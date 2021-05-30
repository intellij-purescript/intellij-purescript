package org.purescript.lexer

class AdoLexerTest: PSLexerTestBase("ado") {
    fun testPureSugar() = doFileTest("purs")
    fun testMapSugar() = doFileTest("purs")
    fun testInWithRecord() = doFileTest("purs")
}