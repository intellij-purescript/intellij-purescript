package org.purescript.module.exports

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import org.purescript.psi.PSPsiFactory

class ExportedTypeOperatorReference(operator: ExportedTypeOperator.Psi) :
    PsiReferenceBase<ExportedTypeOperator.Psi>(operator, operator.symbol.operator.textRangeInParent, false) {
    override fun getVariants() = candidates.toList().toTypedArray()
    override fun resolve() =
        (localCandidates + importedCandidates(element.name)).firstOrNull { it.name == element.name }

    private val importedCandidates
        get() = element.module.imports.asSequence().flatMap { it.importedTypeFixityDeclarations }

    private fun importedCandidates(name: String) =
        element.module.imports.asSequence().flatMap { it.importedTypeFixityDeclarations(name) }

    private val localCandidates get() = element.module.typeFixityDeclarations.asSequence()
    private val candidates get() = localCandidates + importedCandidates

    override fun handleElementRename(name: String): PsiElement? {
        val newName = PSPsiFactory(element.project).createOperatorName(name) ?: return null
        element.symbol.operator.replace(newName)
        return element
    }
}
