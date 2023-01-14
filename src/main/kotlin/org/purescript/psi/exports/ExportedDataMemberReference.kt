package org.purescript.psi.exports

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiReferenceBase
import org.purescript.psi.declaration.data.PSDataDeclaration
import org.purescript.psi.newtype.PSNewTypeDeclaration

class ExportedDataMemberReference(exportedDataMember: PSExportedDataMember) : PsiReferenceBase<PSExportedDataMember>(
    exportedDataMember,
    exportedDataMember.properName.textRangeInParent,
    false
) {
    override fun getVariants(): Array<PsiNamedElement> =
        candidates.distinctBy { it.name }
            .toTypedArray()

    override fun resolve(): PsiElement? =
        candidates.firstOrNull { it.name == myElement.name }

    private val candidates: Array<out PsiNamedElement>
        get() = when (val declaration = myElement.exportedData?.reference?.resolve()) {
            is PSDataDeclaration -> declaration.dataConstructorList?.dataConstructors ?: emptyArray()
            is PSNewTypeDeclaration -> arrayOf(declaration.newTypeConstructor)
            else -> emptyArray()
        }
}
