package org.purescript.lexer

import com.intellij.lexer.EmptyLexer
import com.intellij.psi.tree.IElementType
import junit.framework.TestCase
import org.purescript.parser.PSTokens

class LayoutLexerTest : TestCase() {
    fun testName() {
        val lexer = LayoutLexer(EmptyLexer())
        val tokens = getTokens(lexer,"")
        assertEquals(0, tokens.size)
    }

    fun `test it calculate offset`() {
        val lexer = LayoutLexer(PSLexer())
        val source = """
                module Main where
            """.trimIndent()
        lexer.start(source)
        assertEquals(0, lexer.tokenStart)
        assertEquals(6, lexer.tokenEnd)
        lexer.advance()
        assertEquals(6, lexer.tokenStart)
        assertEquals(7, lexer.tokenEnd)
    }

    fun `test module where creates layout start`() {
        val lexer = LayoutLexer(PSLexer())
        val source = """
                module Main where
            """.trimIndent()
        val tokens = getTokens(lexer, source)
        assertTrue(tokens.toSet().contains(PSTokens.LAYOUT_START))
    }

    fun `test separator token for top level declaration`() {
        val lexer = LayoutLexer(PSLexer())
        val source = """
                module Main where
                f = 1
            """.trimIndent()
        val tokens = getTokens(lexer, source)
        assertEquals(13, tokens.size)
    }

    fun `test do layout`() {
        val lexer = LayoutLexer(PSLexer())
        val tokens = getTokens(
            lexer,
            """
                module Main where
                
                import Effect.Console (log)
                
                main = do
                    log "Hello world"
                    
            """.trimIndent()
        )
        assertEquals(28, tokens.size)
    }
}

private fun getTokens(lexer: LayoutLexer, source: String): List<IElementType> {
    lexer.start(source)
    val generator = generateSequence {
        val tokenType = lexer.tokenType
        lexer.advance()
        tokenType
    }
    return generator.toList()
}