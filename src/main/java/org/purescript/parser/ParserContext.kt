@file:Suppress("unused")

package org.purescript.parser

import com.intellij.lang.PsiBuilder
import com.intellij.lang.WhitespacesAndCommentsBinder
import com.intellij.psi.tree.IElementType
import com.intellij.util.containers.Stack
import org.purescript.psi.PSTokens
import java.util.*

class ParserContext(private val builder: PsiBuilder) {
    var column = 0
        private set
    val indentationLevel = Stack<Int>()
    private val recoverySet = HashMap<IElementType, Int?>()
    var isInAttempt = false
    private var inOptional = 0
    fun eof(): Boolean {
        return builder.eof()
    }

    private inner class PureMarker : PsiBuilder.Marker {
        private val start: Int
        private val marker: PsiBuilder.Marker

        constructor(marker: PsiBuilder.Marker) {
            start = column
            this.marker = marker
        }

        constructor(start: Int, marker: PsiBuilder.Marker) {
            this.start = start
            this.marker = marker
        }

        override fun precede(): PsiBuilder.Marker {
            return PureMarker(start, marker)
        }

        override fun drop() {
            marker.drop()
        }

        override fun rollbackTo() {
            column = start
            marker.rollbackTo()
        }

        override fun done(type: IElementType) {
            marker.done(type)
        }

        override fun collapse(type: IElementType) {
            marker.collapse(type)
        }

        override fun doneBefore(type: IElementType, before: PsiBuilder.Marker) {
            marker.doneBefore(type, before)
        }

        override fun doneBefore(
            type: IElementType,
            before: PsiBuilder.Marker,
            errorMessage: String
        ) {
            marker.doneBefore(type, before, errorMessage)
        }

        override fun error(message: String) {
            marker.error(message)
        }

        override fun errorBefore(message: String, before: PsiBuilder.Marker) {
            marker.errorBefore(message, before)
        }

        override fun setCustomEdgeTokenBinders(
            left: WhitespacesAndCommentsBinder?,
            right: WhitespacesAndCommentsBinder?
        ) {
            marker.setCustomEdgeTokenBinders(left, right)
        }
    }

    fun whiteSpace() {
        while (!builder.eof()) {
            val type = builder.tokenType
            if (type === PSTokens.WS || type === PSTokens.DOC_COMMENT) {
                advance()
            } else {
                break
            }
        }
    }

    fun advance() {
        val text = builder.tokenText
        if (text != null) {
            val type = builder.tokenType
            if (type === PSTokens.STRING || type === PSTokens.WS) {
                for (element in text) {
                    when (element) {
                        '\n' -> column = 0
                        '\t' -> column = column - column % 8 + 8
                        else -> column++
                    }
                }
            } else {
                column += text.length
            }
        }
        builder.advanceLexer()
    }

    fun addUntilToken(token: IElementType) {
        var i = 0
        if (recoverySet.containsKey(token)) {
            i = recoverySet[token]!!
        }
        recoverySet[token] = i + 1
    }

    fun removeUntilToken(token: IElementType) {
        val i = recoverySet[token]!!
        if (i == 1) {
            recoverySet.remove(token)
        } else {
            recoverySet[token] = i - 1
        }
    }

    fun isUntilToken(token: IElementType): Boolean {
        return recoverySet.containsKey(token)
    }

    fun enterOptional() {
        inOptional++
    }

    fun exitOptional() {
        inOptional--
    }

    fun isInOptional(): Boolean {
        return inOptional > 0
    }

    fun text(): String {
        return builder.tokenText ?: return ""
    }

    fun peek(): IElementType {
        val tokenType = builder.tokenType
        return tokenType ?: PSTokens.EOF
    }

    fun eat(type: IElementType): Boolean {
        if (builder.tokenType === type) {
            advance()
            return true
        }
        return false
    }

    fun start(): PsiBuilder.Marker {
        // Consume all the white spaces.
        builder.eof()
        return PureMarker(builder.mark())
    }

    val position: Int
        get() = builder.currentOffset

    val lastIndentationLevel: Int
        get() = if (indentationLevel.size >= 2) {
            indentationLevel[indentationLevel.size - 2]
        } else 0

    fun pushIndentationLevel() {
        indentationLevel.push(column)
    }

    fun popIndentationLevel() {
        indentationLevel.tryPop()
    }

    fun getText(start: Int, end: Int): String {
        return builder.originalText.subSequence(start, end).toString()
    }

    init {
        indentationLevel.push(0)
    }
}
