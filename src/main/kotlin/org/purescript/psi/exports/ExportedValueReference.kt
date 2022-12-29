package org.purescript.psi.exports

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiReferenceBase
import org.purescript.psi.PSPsiFactory

class ExportedValueReference(exportedValue: ExportedValue.Psi) : PsiReferenceBase<ExportedValue.Psi>(
    exportedValue,
    exportedValue.identifier.textRangeInParent,
    false
) {

    override fun getVariants(): Array<PsiNamedElement> =
        candidates.distinctBy { it.name }.toTypedArray()

    override fun resolve(): PsiElement? =
        candidates.firstOrNull { it.name == myElement.name }

    private val candidates: List<PsiNamedElement>
        get() =
            myElement?.module?.run {
                listOf(
                    *cache.valueDeclarations,
                    *cache.foreignValueDeclarations,
                    *cache.classes
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
