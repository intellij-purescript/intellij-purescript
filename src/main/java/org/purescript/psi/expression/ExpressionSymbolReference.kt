package org.purescript.psi.expression

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase

class ExpressionSymbolReference(symbol: PSExpressionSymbol) :
    PsiReferenceBase<PSExpressionSymbol>(
        symbol,
        symbol.qualifiedSymbol.symbol.textRangeInParent.grown(-2).shiftRight(1),
        false
    ) {

    override fun getVariants(): Array<Any> =
        candidates.toList().toTypedArray()

    override fun resolve(): PsiElement? {
        return candidates.firstOrNull() { it.name == element.name}
    }

    val candidates get() = sequence {
        val module = element.module ?: return@sequence
        yieldAll(module.fixityDeclarations.asSequence())
        yieldAll(module.importDeclarations.flatMap { it.importedFixityDeclarations })
    }

}
