package org.purescript.lexer

import com.intellij.lexer.DelegateLexer
import com.intellij.lexer.Lexer
import com.intellij.psi.tree.IElementType
import org.purescript.lexer.token.SourcePos
import org.purescript.lexer.token.SourceRange
import org.purescript.lexer.token.SourceToken
import org.purescript.parser.PSTokens

data class LayoutStack(
    val sourcePos: SourcePos,
    val layoutDelimiter: LayoutDelimiter,
    val tail: LayoutStack?
)

enum class LayoutDelimiter {
    Root,
    TopDecl,
    TopDeclH,
    DeclGuar,
    Case,
    CaseBind,
    CaseGuar,
    LambdaBi,
    Paren,
    Brace,
    Square,
    If,
    Then,
    Property,
    Forall,
    Tick,
    Let,
    LetStmt,
    Where,
    Of,
    Do,
    Ado,
}

fun currentIndent(stk: LayoutStack?): SourcePos? {
    return stk?.let {
        if (isIndented(it.layoutDelimiter)) {
            it.sourcePos
        } else {
            currentIndent(it.tail)
        }
    }
}

fun isIndented(lyt: LayoutDelimiter): Boolean = when(lyt) {
    LayoutDelimiter.Let -> true
    LayoutDelimiter.LetStmt -> true
    LayoutDelimiter.Where -> true
    LayoutDelimiter.Of -> true
    LayoutDelimiter.Do -> true
    LayoutDelimiter.Ado -> true
    else -> false
}
class LayoutLexer(delegate: Lexer) : DelegateLexer(delegate) {

    private var delegatedTokens: Iterator<SourceToken> =
        listOf<SourceToken>().iterator()
    private var token: SourceToken? = null

    override fun start(
        buffer: CharSequence,
        startOffset: Int,
        endOffset: Int,
        initialState: Int
    ) {
        super.start(buffer, startOffset, endOffset, initialState)
        delegatedTokens = generateSequence {
            val sourceToken: SourceToken? = delegate.tokenType?.let { value ->
                SourceToken(
                    SourceRange(
                        start = SourcePos(0, 0, delegate.tokenStart),
                        end = SourcePos(0, 0, delegate.tokenEnd)
                    ),
                    value = value
                )
            }
            delegate.advance()
            sourceToken
        }
            .iterator()
        advance()
    }

    override fun advance() {
        token = when {
             token?.value == PSTokens.WHERE -> layoutStart()
            delegatedTokens.hasNext() -> delegatedTokens.next()
            else -> null
        }
    }

    private fun layoutStart() = SourceToken(
        range = SourceRange(token!!.range.start, token!!.range.start),
        value = PSTokens.LAYOUT_START
    )

    override fun getTokenType(): IElementType? {
        return token?.value
    }
}