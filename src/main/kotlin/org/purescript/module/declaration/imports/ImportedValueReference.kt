package org.purescript.module.declaration.imports

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiReferenceBase
import org.purescript.Find
import org.purescript.psi.PSPsiFactory

class ImportedValueReference(element: PSImportedValue) : PsiReferenceBase<PSImportedValue>(
    element,
    element.identifier.textRangeInParent,
    false
) {
    val project = element.project
    val name = element.name

    override fun getVariants(): Array<PsiNamedElement> =
        candidates.toList().distinctBy { it.name }
            .toTypedArray()
    
    private val candidates: Sequence<PsiNamedElement>
        get() {
            val module = element.importDeclaration.importedModule ?: return emptySequence()
            return module.exportedValueNames.asSequence()
        }
    private fun candidates(name:String) = 
        element.importDeclaration.importedModule?.exportedValue(name) ?: emptySequence()
    

    override fun resolve(): PsiElement? = candidates(element.name).firstOrNull { it.name == element.name }

    override fun handleElementRename(name: String): PsiElement? {
        val newName = PSPsiFactory(project).createIdentifier(name)
            ?: return null
        element.identifier.replace(newName)
        return element
    }
}
