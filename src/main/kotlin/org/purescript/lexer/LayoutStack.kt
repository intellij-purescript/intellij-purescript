package org.purescript.lexer

import org.purescript.lexer.token.SourcePos

data class LayoutStack(
    val sourcePos: SourcePos,
    val layoutDelimiter: LayoutDelimiter,
    val tail: LayoutStack?
) {
    fun isTopDecl(tokPos: SourcePos): Boolean = when {
        tail == null || tail.tail != null -> false
        tail.layoutDelimiter != LayoutDelimiter.Root -> false
        layoutDelimiter != LayoutDelimiter.Where -> false
        else -> tokPos.column == sourcePos.column
    }
    inline fun count(filter: (LayoutDelimiter) -> Boolean): Int {
        var tail = this.tail
        var count = if (filter(layoutDelimiter)) 1 else 0
        while (tail != null) {
            if (filter(tail.layoutDelimiter)) count++
            tail = tail.tail
        }
        return count
    }
    inline fun find(filter: (LayoutDelimiter) -> Boolean): LayoutStack? {
        var stack: LayoutStack? = this
        while (stack != null) {
            if (filter(stack.layoutDelimiter)) return stack
            stack = stack.tail
        }
        return null
    }
}