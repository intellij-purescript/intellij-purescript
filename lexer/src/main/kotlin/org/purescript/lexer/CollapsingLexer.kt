package org.purescript.lexer

import com.intellij.lexer.Lexer
import com.intellij.lexer.MergeFunction
import com.intellij.lexer.MergingLexerAdapterBase
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet

class CollapsingLexer(original: Lexer, val collapse: TokenSet, val to: IElementType): MergingLexerAdapterBase(original) {


    override fun getMergeFunction(): MergeFunction {
        return object : MergeFunction {
            override fun merge(
                type: IElementType,
                originalLexer: Lexer
            ): IElementType {
                if (!collapse.contains(type)) {
                    return type
                }
                while (collapse.contains(originalLexer.tokenType)) {
                    originalLexer.advance()
                }
                return to
            }
        }
    }

}