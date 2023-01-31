package org.purescript.psi.declaration.imports

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult.createResults
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.ResolveResult
import org.purescript.psi.PSPsiFactory

class ImportedValueReference(element: PSImportedValue) : PsiReferenceBase.Poly<PSImportedValue>(
    element,
    element.identifier.textRangeInParent,
    false
) {
    override fun getVariants(): Array<PsiNamedElement> =
        candidates.distinctBy { it.name }
            .toTypedArray()

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> =
        createResults(candidates.filter { it.name == element.name })

    private val candidates: List<PsiNamedElement>
        get() {
            val module = element.importDeclaration?.importedModule
                ?: return emptyList()
            val candidates = mutableListOf<PsiNamedElement>()
            candidates.addAll(module.exportedValueDeclarationGroups)
            candidates.addAll(module.exportedForeignValueDeclarations)
            for (exportedClassDeclaration in module.exportedClassDeclarations) {
                candidates.addAll(exportedClassDeclaration.classMembers)
            }
            return candidates
        }
    override fun handleElementRename(name: String): PsiElement? {
        val newName = PSPsiFactory(element.project).createIdentifier(name)
            ?: return null
        element.identifier.replace(newName)
        return element
    }
}
