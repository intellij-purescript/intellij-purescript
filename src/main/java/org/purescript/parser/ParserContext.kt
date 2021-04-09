@file:Suppress("unused")

package org.purescript.parser

import com.intellij.lang.PsiBuilder
import com.intellij.psi.tree.IElementType

class ParserContext(private val builder: PsiBuilder) {
    private val recoverySet = HashMap<IElementType, Int?>()
    var isInAttempt = false
    private var inOptional = 0
    fun eof() = builder.eof()

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

    fun start(): PsiBuilder.Marker = builder.mark()

    val position: Int
        get() = builder.currentOffset

    fun getText(start: Int, end: Int) =
        builder.originalText.subSequence(start, end).toString()
}
