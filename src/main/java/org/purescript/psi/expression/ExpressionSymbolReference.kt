package org.purescript.psi.expression

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase

class ExpressionSymbolReference(symbol: PSExpressionSymbol) :
    PsiReferenceBase<PSExpressionSymbol>(
        symbol,
        symbol.qualifiedSymbol.symbol.textRangeInParent,
        false
    ) {

    override fun getVariants(): Array<Any> =
        candidates.toList().toTypedArray()

    override fun resolve(): PsiElement? {
        return candidates.firstOrNull()
    }

    val candidates get() =
        element.module
            ?.fixityDeclarations
            ?: arrayOf()

}
