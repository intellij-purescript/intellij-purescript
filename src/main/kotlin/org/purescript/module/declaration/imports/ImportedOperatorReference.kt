package org.purescript.module.declaration.imports

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import org.purescript.module.declaration.fixity.FixityDeclaration
import org.purescript.psi.PSPsiFactory

class ImportedOperatorReference(element: PSImportedOperator) :
    PsiReferenceBase<PSImportedOperator>(element, element.symbol.operator.textRangeInParent, false) {
    override fun getVariants(): Array<Any> = candidates.toList().toTypedArray()
    override fun resolve(): FixityDeclaration? {
        val name = element.name
        return candidates(name).firstOrNull { it.name == name }
    }
    val candidates: Sequence<FixityDeclaration>
        get() = element.importDeclaration.importedModule?.let {
            it.exportedValueFixityDeclarations + it.exportedConstructorFixityDeclarations
        } ?: emptySequence()

    fun candidates(name: String): Sequence<FixityDeclaration> = 
        element.importDeclaration.importedModule?.let {
        it.exportedFixityDeclarations(name) + it.exportedConstructorFixityDeclarations(name)
    } ?: emptySequence()

    override fun handleElementRename(name: String): PsiElement? {
        val newName = PSPsiFactory(element.project).createOperatorName(name) ?: return null
        element.symbol.operator.replace(newName)
        return element
    }
}
