package org.purescript.psi.exports

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiReferenceBase

class ExportedDataReference(exportedData: PSExportedData) : PsiReferenceBase<PSExportedData>(
    exportedData,
    exportedData.properName.textRangeInParent,
    false
) {
    override fun getVariants(): Array<PsiNamedElement> =
        candidates.distinctBy { it.name }
            .toTypedArray()

    override fun resolve(): PsiElement? =
        candidates.firstOrNull { it.name == myElement.name }

    private val candidates: Array<PsiNamedElement>
        get() =
            myElement.module.run {
                arrayOf(
                    *dataDeclarations,
                    *newTypeDeclarations,
                )
            }
}
