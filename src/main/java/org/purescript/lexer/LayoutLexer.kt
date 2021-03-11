package org.purescript.lexer

import com.intellij.lexer.DelegateLexer
import com.intellij.lexer.Lexer
import com.intellij.psi.tree.IElementType
import org.purescript.parser.PSTokens

enum class LayoutDelimiter {
    WHERE
}

data class SourcePos(
    val line: Int,
    val column: Int,
    val offset: Int,
)

data class SourceRange(
    val start: SourcePos,
    val end: SourcePos
)

data class SourceToken(
    val range: SourceRange,
    val value: IElementType
)

data class LayoutStack(
    val sourcePos: SourcePos,
    val layoutDelimiter: LayoutDelimiter,
    val tail: LayoutStack?
)

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
             token?.value == PSTokens.WHERE -> SourceToken(
                range = token!!.range,
                value = PSTokens.LAYOUT_START
            )
            delegatedTokens.hasNext() -> delegatedTokens.next()
            else -> null
        }
    }

    override fun getTokenType(): IElementType? {
        return token?.value
    }
}