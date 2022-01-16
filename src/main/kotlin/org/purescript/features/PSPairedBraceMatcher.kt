package org.purescript.features

import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import org.purescript.parser.PSTokens

class PSPairedBraceMatcher : PairedBraceMatcher {
    override fun getPairs() = PAIRS

    override fun isPairedBracesAllowedBeforeType(
        lbraceType: IElementType,
        contextType: IElementType?
    ) = contextType == null || contextType === PSTokens.WS

    override fun getCodeConstructStart(file: PsiFile, openingBraceOffset: Int) =
        openingBraceOffset
}

private val PAIRS = arrayOf(
    BracePair(PSTokens.LCURLY, PSTokens.RCURLY, true),
    BracePair(PSTokens.LBRACK, PSTokens.RBRACK, true),
    BracePair(PSTokens.LPAREN, PSTokens.RPAREN, false)
)