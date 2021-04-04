package org.purescript.lexer

class DoLexerTest: PSLexerTestBase("do") {
    fun testQualifiedDo() = doFileTest("purs")
}