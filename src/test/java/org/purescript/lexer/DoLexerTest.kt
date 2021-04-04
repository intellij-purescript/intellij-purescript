package org.purescript.lexer

class DoLexerTest: PSLexerTestBase(LayoutLexer(PSLexer()), "do") {
    fun testQualifiedDo() = doFileTest("purs")
}