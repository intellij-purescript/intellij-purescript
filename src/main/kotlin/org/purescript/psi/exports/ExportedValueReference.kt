package org.purescript.psi.exports

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult.createResults
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.ResolveResult
import org.purescript.psi.PSPsiFactory

class ExportedValueReference(exportedValue: PSExportedValue) : PsiReferenceBase.Poly<PSExportedValue>(
    exportedValue,
    exportedValue.identifier.textRangeInParent,
    false
) {

    override fun getVariants(): Array<PsiNamedElement> {
        return candidates.distinctBy { it.name }
            .toTypedArray()
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        return createResults(*candidates.filter { it.name == myElement.name }.toTypedArray())
    }

    private val candidates: List<PsiNamedElement>
        get() =
            myElement?.module?.run {
                listOf(
                    *cache.valueDeclarations,
                    *cache.foreignValueDeclarations,
                    *cache.classDeclarations
                        .flatMap { it.classMembers.toList() }
                        .toTypedArray()
                )
            } ?: emptyList()

    override fun handleElementRename(name: String): PsiElement? {
        val newName = PSPsiFactory(element.project).createIdentifier(name)
            ?: return null
        element.identifier.replace(newName)
        return element
    }
}
