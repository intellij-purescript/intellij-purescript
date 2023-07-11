package org.purescript.module.exports

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import org.purescript.module.declaration.fixity.FixityDeclaration
import org.purescript.psi.PSPsiFactory

class ExportedOperatorReference(typeOperator: ExportedOperator.Psi) :
    PsiReferenceBase<ExportedOperator.Psi>(typeOperator, typeOperator.symbol.operator.textRangeInParent, false) {
    private val imports get() = element.module.imports
    override fun getVariants() = candidates.toList().toTypedArray()
    override fun resolve(): FixityDeclaration? {
        val local = localCandidates.toList()
        val imported = importedCandidates(element.name).toList()
        return (local + imported).firstOrNull { it.name == element.name }
    }

    private val importedCandidates
        get() = sequence<FixityDeclaration> {
            yieldAll(imports.asSequence().flatMap { it.importedValueFixityDeclarations })
            yieldAll(imports.asSequence().flatMap { it.importedConstructorFixityDeclarations })
        }

    private fun importedCandidates(name: String) = imports.asSequence()
        .flatMap { it.importedFixityDeclarations(name) }

    private val localCandidates
        get() = sequence<FixityDeclaration> {
            yieldAll(element.module.valueFixityDeclarations.asSequence())
            yieldAll(element.module.constructorFixityDeclarations.asSequence())
        }
    private val candidates get() = localCandidates + importedCandidates

    override fun handleElementRename(name: String): PsiElement? {
        val newName = PSPsiFactory(element.project).createOperatorName(name) ?: return null
        element.symbol.operator.replace(newName)
        return element
    }
}
