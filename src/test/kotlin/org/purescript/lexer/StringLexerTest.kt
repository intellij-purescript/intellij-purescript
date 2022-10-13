package org.purescript.lexer

class StringLexerTest: PSLexerTestBase("string") {
    fun testStringWithTab() = doFileTest("purs")
    fun testTripleQuoteString() = doFileTest("purs")
}