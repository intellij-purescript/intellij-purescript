package org.purescript.psi.declaration.imports

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiReferenceBase
import org.purescript.psi.PSPsiFactory

class ImportedValueReference(element: PSImportedValue) : PsiReferenceBase<PSImportedValue>(
    element,
    element.identifier.textRangeInParent,
    false
) {
    override fun getVariants(): Array<PsiNamedElement> =
        candidates.toList().distinctBy { it.name }
            .toTypedArray()
    
    private val candidates: Sequence<PsiNamedElement>
        get() {
            val module = element.importDeclaration?.importedModule
                ?: return emptySequence()
            return sequence {
                yieldAll(module.exportedValueDeclarationGroups)
                yieldAll(module.exportedForeignValueDeclarations)
                for (exportedClassDeclaration in module.exportedClassDeclarations) {
                    yieldAll(exportedClassDeclaration.classMembers.asSequence())
                }
            }
        }

    override fun resolve(): PsiElement? = candidates.firstOrNull { it.name == element.name }

    override fun handleElementRename(name: String): PsiElement? {
        val newName = PSPsiFactory(element.project).createIdentifier(name)
            ?: return null
        element.identifier.replace(newName)
        return element
    }
}
