package org.purescript.lexer

import org.purescript.lexer.LayoutDelimiter.*
import org.purescript.lexer.token.SourcePos

data class LayoutState(
    val stack: LayoutStack,
    val acc: List<SuperToken>
) {
    fun isTopDecl(tokPos: SourcePos) = stack.isTopDecl(tokPos)
    inline fun collapse(tokPos: SourcePos, p: (LayoutDelimiter) -> Boolean) =
        collapse(tokPos) { _, _, lyt -> p(lyt) }

    fun insertStart(nextPos: SourcePos, lyt: LayoutDelimiter): LayoutState =
        when (val indent = stack.find { it.isIndent }) {
            null -> pushStack(nextPos, lyt).insertToken(nextPos.asStart)
            else -> when {
                nextPos.column <= indent.sourcePos.column -> this
                else -> pushStack(nextPos, lyt).insertToken(nextPos.asStart)
            }
        }

    inline fun insertKwProperty(
        t: SuperToken,
        k: (LayoutState) -> LayoutState
    ): LayoutState {
        var stack1 = stack
        val acc1 = acc.toMutableList()
        while (
            stack1.tail != null &&
            stack1.layoutDelimiter.isIndent &&
            t.start.column < stack1.sourcePos.column
        ) {
            stack1 = stack1.pop()
            acc1 += t.start.asEnd
        }
        when {
            t.start.column != stack1.sourcePos.column ||
                t.start.line == stack1.sourcePos.line -> Unit

            TopDecl == stack1.layoutDelimiter ||
                TopDeclHead == stack1.layoutDelimiter -> {
                stack1 = stack1.pop()
                acc1 += t.start.asSep
            }

            Of == stack1.layoutDelimiter -> {
                stack1 = stack1.push(t.start, CaseBinders)
                acc1 += t.start.asSep
            }

            stack1.layoutDelimiter.isIndent -> {
                acc1 += t.start.asSep
            }
        }
        return when (stack.layoutDelimiter) {
            Property -> LayoutState(stack1.pop(), acc1 + (t))
            else -> k(LayoutState(stack1, acc1 + (t)))
        }
    }

    inline fun collapse(
        tokPos: SourcePos,
        p: (SourcePos, SourcePos, LayoutDelimiter) -> Boolean
    ): LayoutState {
        var (stack, acc) = this
        while (
            stack.tail != null &&
            p(tokPos, stack.sourcePos, stack.layoutDelimiter)
        ) {
            if (stack.layoutDelimiter.isIndent) {
                acc = acc + tokPos.asEnd
            }
            stack = stack.pop()
        }
        return LayoutState(stack, acc)
    }

    fun insertToken(token: SuperToken) = copy(acc = acc + token)
    fun pushStack(lytPos: SourcePos, lyt: LayoutDelimiter) =
        copy(stack = stack.push(lytPos, lyt))

    fun insertDefault(src: SuperToken): LayoutState {
        var stack = stack
        val acc = acc.toMutableList()
        while (
            stack.tail != null &&
            stack.layoutDelimiter.isIndent &&
            src.start.column < stack.sourcePos.column
        ) {
            stack = stack.pop()
            acc += src.start.asEnd
        }
        when {
            src.start.column != stack.sourcePos.column ||
                src.start.line == stack.sourcePos.line -> Unit

            TopDecl == stack.layoutDelimiter ||
                TopDeclHead == stack.layoutDelimiter -> {
                stack = stack.pop()
                acc += src.start.asSep
            }

            Of == stack.layoutDelimiter -> {
                stack = stack.push(src.start, CaseBinders)
                acc += src.start.asSep
            }

            stack.layoutDelimiter.isIndent -> {
                acc += src.start.asSep
            }
        }
        return LayoutState(stack, acc + (src))
    }

    inline fun popStack(p: (LayoutDelimiter) -> Boolean): LayoutState = when {
        p(stack.layoutDelimiter) -> copy(stack = stack.pop())
        else -> this
    }

    fun insertSep(tokPos: SourcePos) = when {
        tokPos.column != stack.sourcePos.column ||
            tokPos.line == stack.sourcePos.line -> this

        TopDecl == stack.layoutDelimiter ||
            TopDeclHead == stack.layoutDelimiter -> {
            val popped = stack.pop()
            LayoutState(popped, acc + (tokPos.asSep))
        }

        Of == stack.layoutDelimiter -> LayoutState(
            stack.push(tokPos, CaseBinders),
            acc + (tokPos.asSep)
        )

        stack.layoutDelimiter.isIndent ->
            copy(acc = acc + (tokPos.asSep))

        else -> this
    }

    fun toPair(): Pair<LayoutStack, List<SuperToken>> {
        return stack to acc
    }
}