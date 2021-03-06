package org.purescript.lexer

import com.intellij.lang.impl.TokenSequence
import com.intellij.lexer.EmptyLexer
import junit.framework.TestCase

class LayoutLexerTest : TestCase() {
    fun testName() {
        val lexer = LayoutLexer(EmptyLexer())
        val tokens = TokenSequence.performLexing("", lexer)
        assertEquals(0, tokens.tokenCount)
    }
}