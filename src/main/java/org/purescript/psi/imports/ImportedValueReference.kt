package org.purescript.psi.imports

import com.intellij.psi.PsiElementResolveResult.createResults
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.ResolveResult

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

            return module.exportedValueDeclarations + module.exportedForeignValueDeclarations
        }
}
