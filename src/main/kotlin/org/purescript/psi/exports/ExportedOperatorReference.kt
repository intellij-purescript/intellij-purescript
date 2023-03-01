package org.purescript.psi.exports

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import org.purescript.psi.PSPsiFactory

class ExportedOperatorReference(operator: ExportedOperator.Psi) :
    PsiReferenceBase<ExportedOperator.Psi>(operator, operator.symbol.operator.textRangeInParent, false) {
    override fun getVariants() = candidates.toList().toTypedArray()
    override fun resolve() = (localCandidates + importedCandidates(element.name)).firstOrNull { it.name == element.name }

    private val importedCandidates
        get() =
            element.module?.cache?.imports
                ?.flatMap { it.importedFixityDeclarations }
                ?.asSequence()
                ?: sequenceOf()
    
    private fun importedCandidates(name : String) =
            element.module?.cache?.imports?.asSequence()
                ?.flatMap { it.importedFixityDeclarations(name) }
                ?: sequenceOf()

    private val localCandidates
        get() = element.module?.fixityDeclarations
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
