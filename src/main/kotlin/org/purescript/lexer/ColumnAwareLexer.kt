package org.purescript.lexer

import com.intellij.lexer.FlexAdapter

class ColumnAwareLexer: FlexAdapter(_PSLexer()) {

    private var columns: ShortArray = ShortArray(256)
    fun getColumn(offset: Int):Int = columns[offset].toInt()

    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
        val size = endOffset + 1
        columns = if (size >= columns.size) columns.copyOf(size) else columns
        super.start(buffer, startOffset, endOffset, initialState)
    }

    override fun advance() {
        super.advance()
        val offset = currentPosition.offset
        val column = (flex as _PSLexer).column
        assert(offset < columns.size)
        val shortColumn = column.toShort()
        assert(shortColumn.toInt() == column)
        columns[offset] = shortColumn
    }
}