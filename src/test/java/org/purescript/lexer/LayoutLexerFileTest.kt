package org.purescript.lexer

class LayoutLexerFileTest: PSLexerTestBase(LayoutLexer(PSLexer()), "lexer") {
    fun testQualifiedDo() = doFileTest("purs")
}