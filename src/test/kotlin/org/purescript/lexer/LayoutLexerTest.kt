package org.purescript.lexer

import com.intellij.psi.TokenType.WHITE_SPACE
import junit.framework.TestCase
import org.purescript.lexer.token.SourceToken
import org.purescript.parser.MODULE

class LayoutLexerTest : TestCase() {
    private val psLexer = PSLexer()
    private val root = SourceToken(
        WHITE_SPACE,
        posFromOffset(0),
        posFromOffset(0)
    )

    fun `test it sets correct column for first token`() {
        val source = "module"
        val module =
            SourceToken(MODULE, posFromOffset(0), posFromOffset(6))
        val first = correctLineAndColumn(source)(root, module)
        assertEquals(0, first.start.column)
        assertEquals(6, first.end.column)
    }

    fun `test it sets correct column for second token`() {
        val source = "module "
        psLexer.start(source)
        val tokens = getTokens(psLexer)
        val corrected =
            tokens.runningFold(root, correctLineAndColumn(source)).drop(1)
        val (_, second) = corrected.toList()
        assertEquals(6, second.start.column)
        assertEquals(7, second.end.column)
    }

    fun `test it handle newlines`() {
        val source = """
            module
            Main
            
             where""".trimIndent()
        psLexer.start(source)
        val tokens = getTokens(psLexer)

        val corrected =
            tokens.runningFold(root, correctLineAndColumn(source)).drop(1)

        val (moduleKeyword, newline, moduleName, _, where) =
            corrected.toList()

        assertEquals(moduleKeyword.end, newline.start)
        assertEquals(1, newline.end.line)
        assertEquals(0, newline.end.column)

        assertEquals(newline.end, moduleName.start)
        assertEquals(1, moduleName.end.line)
        assertEquals(4, moduleName.end.column)
        
        assertEquals(3, where.end.line)
        assertEquals(6, where.end.column)
    }

    fun `test it calculate offset`() {
        val lexer = LayoutLexer(psLexer)
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

    fun `test it calculates text`() {
        val lexer = LayoutLexer(psLexer)
        val source = """
                module Main where
            """.trimIndent()
        lexer.start(source)
        assertEquals("module", lexer.tokenText)
        lexer.advance()
        assertEquals(" ", lexer.tokenText)
    }
}