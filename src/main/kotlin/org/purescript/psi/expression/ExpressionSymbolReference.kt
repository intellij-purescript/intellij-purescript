package org.purescript.psi.expression

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import org.purescript.psi.PSPsiElement
import org.purescript.psi.name.PSOperatorName

class ExpressionSymbolReference(
    symbol: PSPsiElement, operator: PSOperatorName
) :
    PsiReferenceBase<PSPsiElement>(
        symbol,
        operator.textRangeInParent,
        false
    ) {

    override fun getVariants(): Array<Any> =
        candidates.toList().toTypedArray()

    override fun resolve(): PsiElement? {
        return candidates.firstOrNull { it.name == element.name}
    }

    val candidates get() = sequence {
        val module = element.module ?: return@sequence
        yieldAll(module.fixityDeclarations.asSequence())
        yieldAll(module.importDeclarations.flatMap { it.importedFixityDeclarations })
    }

}
