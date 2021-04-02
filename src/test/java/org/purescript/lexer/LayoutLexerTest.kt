package org.purescript.lexer

import com.intellij.lexer.EmptyLexer
import com.intellij.psi.tree.IElementType
import junit.framework.TestCase
import org.purescript.lexer.token.SourceToken
import org.purescript.parser.PSTokens

class LayoutLexerTest : TestCase() {
    private val psLexer = PSLexer()
    private val root = SourceToken(rangeFromOffsets(0, 0), PSTokens.WS)

    fun testName() {
        val lexer = LayoutLexer(EmptyLexer())
        val tokens = getTokens(lexer, "")
        assertEquals(0, tokens.size)
    }

    fun `test it sets correct column for first token`() {
        val source = "module"
        val module = SourceToken(rangeFromOffsets(0, 6), PSTokens.MODULE)
        val first = correctLineAndColumn(source)(root, module)
        val (start, end) = first.range
        assertEquals(0, start.column)
        assertEquals(6, end.column)
    }

    fun `test it sets correct column for second token`() {
        val source = "module "
        psLexer.start(source)
        val tokens = getTokens(psLexer)
        val corrected =
            tokens.runningFold(root, correctLineAndColumn(source)).drop(1)
        val (_, second) = corrected.toList()
        val (start, end) = second.range
        assertEquals(6, start.column)
        assertEquals(7, end.column)
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

        val (newlineStart, newlineEnd) = newline.range
        assertEquals(moduleKeyword.range.end, newlineStart)
        assertEquals(1, newlineEnd.line)
        assertEquals(0, newlineEnd.column)

        val (moduleNameStart, moduleNameEnd) = moduleName.range
        assertEquals(newlineEnd, moduleNameStart)
        assertEquals(1, moduleNameEnd.line)
        assertEquals(4, moduleNameEnd.column)

        val (whereStart, whereEnd) = where.range
        assertEquals(3, whereEnd.line)
        assertEquals(6, whereEnd.column)
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

    fun `test module where creates layout start`() {
        val lexer = LayoutLexer(psLexer)
        val source = """
                module Main where
            """.trimIndent()
        val tokens = getTokens(lexer, source)
        assertTrue(tokens.toSet().contains(PSTokens.LAYOUT_START))
    }

    fun `test separator token for top level declaration`() {
        val lexer = LayoutLexer(psLexer)
        val source = """
                module Main where
                f = 1
            """.trimIndent()
        val tokens = getTokens(lexer, source)
        assertEquals(13, tokens.size)
    }

    fun `test do layout`() {
        val lexer = LayoutLexer(psLexer)
        val tokens = getTokens(
            lexer,
            """
                module Main where
                
                import Effect.Console (log)
                
                main = do
                    log "Hello world"
                    
            """.trimIndent()
        )
        assertEquals(30, tokens.size)
    }


    fun `test tokensToTokenStep returns the tokens as token steps`() {
        val pos1 = posFromOffset(1)
        val pos2 = posFromOffset(2)
        val pos3 = posFromOffset(3)
        val stack1 = LayoutStack(pos1, LayoutDelimiter.Root, null)
        val stack2 = LayoutStack(pos2, LayoutDelimiter.Root, null)
        val tail = listOf<TokenStep>()
        val tokens = listOf(
            lytToken(pos1, PSTokens.LAYOUT_START) to stack1,
            lytToken(pos2, PSTokens.LAYOUT_END) to stack2,
        )

        val sequence = tokensToTokenStep(tokens, pos3)

        val allExpected = listOf<TokenStep>(
            TokenStep(tokens[0].first, pos2, stack1),
            TokenStep(tokens[1].first, pos3, stack2),
        )
        val expected = allExpected.first()
        val allActual = sequence.toList()
        val actual = allActual.first()
        assertEquals(expected.token, actual.token)
        assertEquals(expected.stack, actual.stack)
        assertEquals(expected.pos, actual.pos)
        assertEquals(allExpected, allActual)
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