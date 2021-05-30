package org.purescript.lexer.token

import junit.framework.TestCase

class SourcePosTest : TestCase() {
    fun `test it sets the new offset`() {
        val sourcePos = SourcePos(0, 0, 0)
        val nextOffset = 10
        val nextSourcePos = sourcePos.bump("0123456789", nextOffset)
        assertEquals(nextOffset, nextSourcePos.offset)
    }

    fun `test it updates lines depending on newlines`() {
        val sourcePos = SourcePos(0, 0, 0)
        assertEquals(1, sourcePos.bump("\n", 1).line)
        assertEquals(0, sourcePos.bump("\n", 0).line)
    }

    fun `test it updates lines depending on newlines and the current line`() {
        val sourcePos = SourcePos(1, 0, 1)
        assertEquals(2, sourcePos.bump("\n\n", 2).line)
        assertEquals(1, sourcePos.bump("\n\n", 1).line)
    }

    fun `test it updates column depending chars since last char`() {
        val sourcePos = SourcePos(0, 0, 0)
        val nextOffset = 10
        val nextSourcePos = sourcePos.bump("0123456789", nextOffset)
        assertEquals(nextOffset, nextSourcePos.column)
    }

    fun `test it resets column if there is a newline`() {
        val sourcePos = SourcePos(0, 0, 0)
        val nextSourcePos = sourcePos.bump("01234567\n89", 11)
        assertEquals(2, nextSourcePos.column)
    }


}