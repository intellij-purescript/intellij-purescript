package org.purescript.lexer

class InstanceLexerTest: PSLexerTestBase("instance") {
    fun testShowBoolean() = doFileTest("purs")
}