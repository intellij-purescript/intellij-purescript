package org.purescript.psi.exports

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import org.purescript.psi.PSPsiFactory

class ExportedOperatorReference(operator: PSExportedOperator) :
    PsiReferenceBase<PSExportedOperator>(
        operator,
        operator.symbol.operator.textRangeInParent,
        false
    ) {
    override fun getVariants() = candidates.toList().toTypedArray()
    override fun resolve() = candidates.firstOrNull { it.name == element.name }

    private val importedCandidates
        get() =
            element.module
                ?.let { it.cache.importDeclarations }
                ?.flatMap { it.importedFixityDeclarations }
                ?.asSequence()
                ?: sequenceOf()

    private val localCandidates
        get() = element.module
            ?.let { it.cache.fixityDeclarations }
            ?.asSequence()
            ?: sequenceOf()

    private val candidates get() = localCandidates + importedCandidates

    override fun handleElementRename(name: String): PsiElement? {
        val newName = PSPsiFactory(element.project).createOperatorName(name)
            ?: return null
        element.symbol.operator.replace(newName)
        return element
    }
}
