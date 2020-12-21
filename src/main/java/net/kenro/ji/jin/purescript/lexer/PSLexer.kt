package net.kenro.ji.jin.purescript.lexer

import com.intellij.lexer.FlexAdapter
import com.intellij.lexer.LookAheadLexer
import com.intellij.lexer.MergeFunction
import com.intellij.lexer.MergingLexerAdapterBase
import net.kenro.ji.jin.purescript.psi.PSTokens

class PSLexer : LookAheadLexer(MergedPureLexer(), 64) {
    private class MergedPureLexer :
        MergingLexerAdapterBase(FlexAdapter(_PSLexer(null))) {
        override fun getMergeFunction(): MergeFunction {
            return Companion.mergeFunction
        }

        companion object {
            private val mergeFunction = MergeFunction { type, originalLexer ->
                var type = type
                if (type === PSTokens.STRING) {
                    while (true) {
                        val tokenType = originalLexer.tokenType
                        if (tokenType !== PSTokens.STRING && tokenType !== PSTokens.STRING_ESCAPED && tokenType !== PSTokens.STRING_GAP) break
                        originalLexer.advance()
                    }
                } else if (type === PSTokens.MLCOMMENT || type === PSTokens.SLCOMMENT || type === PSTokens.WS) {
                    while (true) {
                        type = originalLexer.tokenType!!
                        if (type === PSTokens.MLCOMMENT || type === PSTokens.SLCOMMENT || type === PSTokens.WS) {
                            originalLexer.advance()
                        } else {
                            break
                        }
                    }
                    type = PSTokens.WS
                }
                type
            }
        }
    }
}