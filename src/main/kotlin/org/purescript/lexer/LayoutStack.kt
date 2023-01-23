package org.purescript.lexer

import org.purescript.lexer.token.SourcePos

data class LayoutStack(
    val sourcePos: SourcePos,
    val layoutDelimiter: LayoutDelimiter,
    val tail: LayoutStack?
) {
    inline fun count(filter: (LayoutDelimiter) -> Boolean): Int {
        var tail = this.tail
        var count = if (filter(layoutDelimiter)) 1 else 0
        while (tail != null) {
            if (filter(tail.layoutDelimiter)) count++
            tail = tail.tail
        }
        return count
    }
    fun find(filter: (LayoutStack) -> Boolean): LayoutStack? = when {
        filter(this) -> this
        else -> tail?.find(filter)
    }
}