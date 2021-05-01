package org.purescript.psi.exports

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiReferenceBase

class ExportedOperatorReference(operator: PSExportedOperator) : PsiReferenceBase<PSExportedOperator>(
    operator,
    operator.symbol.operator.textRangeInParent,
    false
) {
    override fun getVariants(): Array<PsiNamedElement> {
        return candidates.toTypedArray()
    }

    override fun resolve(): PsiElement? {
        return  candidates.firstOrNull { it.name == element.name }
    }


    private val candidates: List<PsiNamedElement>
        get() =
            element.module
                ?.fixityDeclarations
                ?.toList()
                ?: listOf()
}
