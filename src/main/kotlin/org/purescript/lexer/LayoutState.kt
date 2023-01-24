package org.purescript.lexer

import org.purescript.lexer.LayoutDelimiter.*
import org.purescript.lexer.token.SourcePos

data class LayoutState(
    val stack: LayoutStack,
    val acc: List<Pair<SuperToken, LayoutStack>>
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
    ) = when (stack.layoutDelimiter) {
        Property -> {
            val insertDefault = insertDefault(t)
            insertDefault.copy(stack = insertDefault.stack.pop())
        }

        else -> k(insertDefault(t))
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
                acc = acc + (tokPos.asEnd to (stack.tail as LayoutStack))
            }
            stack = stack.pop()
        }
        return LayoutState(stack, acc)
    }

    fun insertToken(token: SuperToken) = copy(acc = acc + (token to stack))
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
            acc += (src.start.asEnd to (stack.tail as LayoutStack))
            stack = stack.pop()
        }
        when {
            src.start.column != stack.sourcePos.column ||
                src.start.line == stack.sourcePos.line -> Unit

            TopDecl == stack.layoutDelimiter ||
                TopDeclHead == stack.layoutDelimiter -> {
                stack = stack.pop()
                acc += (src.start.asSep to stack.pop())
            }

            Of == stack.layoutDelimiter -> {
                stack = stack.push(src.start, CaseBinders)
                acc += (src.start.asSep to stack)
            }

            stack.layoutDelimiter.isIndent -> {
                acc += (src.start.asSep to stack)
            }
        }
        return LayoutState(stack, acc + (src to stack))
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
            LayoutState(popped, acc + (tokPos.asSep to popped))
        }

        Of == stack.layoutDelimiter -> LayoutState(
            stack.push(tokPos, CaseBinders),
            acc + (tokPos.asSep to stack)
        )

        stack.layoutDelimiter.isIndent ->
            copy(acc = acc + (tokPos.asSep to stack))

        else -> this
    }

    fun toPair(): Pair<LayoutStack, List<SuperToken>> {
        return stack to acc.map { it.first }
    }
}