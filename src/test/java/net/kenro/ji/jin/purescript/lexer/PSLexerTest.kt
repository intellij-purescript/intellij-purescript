package net.kenro.ji.jin.purescript.lexer

import junit.framework.TestCase

class PSLexerTest : TestCase() {
    fun testItHandlesFileEndingInEmptyLine() {
        val lexer = PSLexer()
        lexer.start("""
            module Main where
            
        """.trimIndent())
        while (lexer.tokenEnd < lexer.bufferEnd) {
            lexer.advance()
        }
    }
}