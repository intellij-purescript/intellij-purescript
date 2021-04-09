@file:Suppress("unused")

package org.purescript.parser

import com.intellij.lang.PsiBuilder
import com.intellij.lang.WhitespacesAndCommentsBinder
import com.intellij.psi.tree.IElementType

class ParserContext(private val builder: PsiBuilder) {
    private val recoverySet = HashMap<IElementType, Int?>()
    var isInAttempt = false
    private var inOptional = 0
    fun eof() = builder.eof()

    private inner class PureMarker(private val marker: PsiBuilder.Marker) :
        PsiBuilder.Marker {

        override fun precede(): PsiBuilder.Marker = PureMarker(marker)
        override fun drop() = marker.drop()
        override fun rollbackTo() = marker.rollbackTo()
        override fun done(type: IElementType) = marker.done(type)
        override fun collapse(type: IElementType) = marker.collapse(type)
        override fun doneBefore(type: IElementType, before: PsiBuilder.Marker) =
            marker.doneBefore(type, before)
        override fun doneBefore(
            type: IElementType,
            before: PsiBuilder.Marker,
            errorMessage: String
        ) = marker.doneBefore(type, before, errorMessage)

        override fun error(message: String) = marker.error(message)

        override fun errorBefore(message: String, before: PsiBuilder.Marker) =
            marker.errorBefore(message, before)

        override fun setCustomEdgeTokenBinders(
            left: WhitespacesAndCommentsBinder?,
            right: WhitespacesAndCommentsBinder?
        ) = marker.setCustomEdgeTokenBinders(left, right)
    }

    fun advance() = builder.advanceLexer()

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

    fun isUntilToken(token: IElementType) = recoverySet.containsKey(token)
    fun enterOptional() = inOptional++
    fun exitOptional() = inOptional--
    fun isInOptional() = inOptional > 0
    fun text() = builder.tokenText ?: ""
    fun peek() = builder.tokenType ?: PSTokens.EOF

    fun eat(type: IElementType): Boolean {
        if (builder.tokenType === type) {
            advance()
            return true
        }
        return false
    }

    fun start(): PsiBuilder.Marker = PureMarker(builder.mark())

    val position: Int
        get() = builder.currentOffset

    fun getText(start: Int, end: Int) =
        builder.originalText.subSequence(start, end).toString()
}
