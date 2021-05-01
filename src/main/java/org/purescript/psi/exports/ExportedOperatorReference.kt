package org.purescript.psi.exports

import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiReferenceBase

class ExportedOperatorReference(operator: PSExportedOperator) :
    PsiReferenceBase<PSExportedOperator>(
        operator,
        operator.symbol.operator.textRangeInParent,
        false
    ) {
    override fun getVariants(): Array<PsiNamedElement> {
        return candidates.toList().toTypedArray()
    }

    override fun resolve() = candidates.firstOrNull { it.name == element.name }

    private val importedCandidates
        get() =
            element.module
                ?.importDeclarations
                ?.flatMap { it.importedFixityDeclarations }
                ?.asSequence()
                ?: sequenceOf()

    private val localCandidates
        get() = element.module
            ?.fixityDeclarations
            ?.asSequence()
            ?: sequenceOf()

    private val candidates get() = localCandidates + importedCandidates
}
