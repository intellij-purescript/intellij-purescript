package org.purescript.lexer

class PSLayoutLexerTest: PSLexerTestBase("layout") {
    fun testSimple() = doFileTest("purs")
}