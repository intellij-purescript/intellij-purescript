package org.purescript.features

import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.TokenType.WHITE_SPACE
import com.intellij.psi.tree.IElementType
import org.purescript.parser.*

class PSPairedBraceMatcher : PairedBraceMatcher {
    override fun getPairs(): Array<BracePair> = PAIRS

    override fun isPairedBracesAllowedBeforeType(
        lbraceType: IElementType,
        contextType: IElementType?
    ): Boolean {
        return (contextType == null
            || contextType === WHITE_SPACE)
    }

    override fun getCodeConstructStart(
        file: PsiFile,
        openingBraceOffset: Int
    ): Int {
        return openingBraceOffset
    }

    companion object {
        private val PAIRS = arrayOf(
            BracePair(LCURLY, RCURLY, true),
            BracePair(LBRACK, RBRACK, true),
            BracePair(LPAREN, RPAREN, false)
        )
    }
}