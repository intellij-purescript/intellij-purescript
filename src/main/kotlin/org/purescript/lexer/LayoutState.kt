package org.purescript.lexer

import org.purescript.lexer.LayoutDelimiter.*
import org.purescript.lexer.token.SourcePos

data class LayoutState(
    val stack: LayoutStack,
    val acc: List<SuperToken>
) {
    inline fun collapse(token: SuperToken, p: (LayoutDelimiter) -> Boolean) =
        collapse(token) { _, _, lyt -> p(lyt) }

    fun insertStart(token: SuperToken, lyt: LayoutDelimiter): LayoutState =
        when (val indent = stack.find { it.endsByDedent }) {
            null -> pushStack(token.end, lyt).insertToken(token.asStart)
            else -> when {
                token.end.column <= indent.column -> this
                else -> pushStack(token.end, lyt).insertToken(token.asStart)
            }
        }

    inline fun insertKwProperty(t: SuperToken, k: (LayoutState) -> LayoutState): LayoutState {
        var stack1 = stack
        val acc1 = acc.toMutableList()
        while (
            stack1.tail != null &&
            stack1.endsByDedent &&
            t.column < stack1.column
        ) {
            stack1 = stack1.pop()
            acc1 += t.asEnd
        }
        when {
            t.column != stack1.column || t.line == stack1.line -> Unit
            TopDecl == stack1.layoutDelimiter || TopDeclHead == stack1.layoutDelimiter -> {
                stack1 = stack1.pop()
                acc1 += t.asSep
            }

            Of == stack1.layoutDelimiter -> {
                stack1 = stack1.push(t.start, CaseBinders)
                acc1 += t.asSep
            }

            stack1.endsByDedent -> acc1 += t.asSep
        }
        return when (stack.layoutDelimiter) {
            Property -> LayoutState(stack1.pop(), acc1 + (t))
            else -> k(LayoutState(stack1, acc1 + (t)))
        }
    }

    inline fun collapse(token: SuperToken, p: (SuperToken, SourcePos, LayoutDelimiter) -> Boolean): LayoutState {
        var (stack, acc) = this
        while (stack.tail != null && p(token, stack.sourcePos, stack.layoutDelimiter)) {
            if (stack.endsByDedent) {
                acc = acc + token.asEnd
            }
            stack = stack.pop()
        }
        return LayoutState(stack, acc)
    }

    fun insertToken(token: SuperToken) = copy(acc = acc + token)
    fun pushStack(lytPos: SourcePos, lyt: LayoutDelimiter) = copy(stack = stack.push(lytPos, lyt))
    fun insertDefault(src: SuperToken): LayoutState {
        var stack = stack
        val acc = acc.toMutableList()
        while (
            stack.tail != null &&
            stack.endsByDedent &&
            src.column < stack.column
        ) {
            stack = stack.pop()
            acc += src.asEnd
        }
        when {
            src.column != stack.column || src.line == stack.line -> Unit
            TopDecl == stack.layoutDelimiter || TopDeclHead == stack.layoutDelimiter -> {
                stack = stack.pop()
                acc += src.asSep
            }

            Of == stack.layoutDelimiter -> {
                stack = stack.push(src.start, CaseBinders)
                acc += src.asSep
            }

            stack.endsByDedent -> acc += src.asSep
        }
        return LayoutState(stack, acc + src)
    }

    inline fun popStack(p: (LayoutDelimiter) -> Boolean): LayoutState = when {
        p(stack.layoutDelimiter) -> copy(stack = stack.pop())
        else -> this
    }
    fun popStack(): LayoutState = copy(stack = stack.pop())
    

    fun insertSep(tokPos: SourcePos) = when {
        tokPos.column != stack.column || tokPos.line == stack.line -> this
        TopDecl == stack.layoutDelimiter || TopDeclHead == stack.layoutDelimiter ->
            LayoutState(stack.pop(), acc + tokPos.asSep)

        Of == stack.layoutDelimiter -> LayoutState(
            stack.push(tokPos, CaseBinders),
            acc + (tokPos.asSep)
        )

        stack.endsByDedent -> copy(acc = acc + tokPos.asSep)
        else -> this
    }

    fun toPair(): Pair<LayoutStack, List<SuperToken>> = stack to acc
}