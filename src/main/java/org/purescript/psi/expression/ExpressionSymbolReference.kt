package org.purescript.psi.expression

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase

class ExpressionSymbolReference(symbol: PSExpressionSymbol) :
    PsiReferenceBase<PSExpressionSymbol>(
        symbol,
        symbol.qualifiedSymbol.symbol.textRangeInParent,
        false
    ) {
    override fun resolve(): PsiElement? {
        TODO("Not yet implemented")
    }

}
