package org.purescript.psi.imports

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiReferenceBase

class ImportedDataReference(element: PSImportedData) : PsiReferenceBase<PSImportedData>(
    element,
    element.properName.textRangeInParent,
    false
) {

    override fun getVariants(): Array<Any> =
        candidates.toTypedArray()

    override fun resolve(): PsiElement? =
        candidates.firstOrNull { it.name == element.name }

    private val candidates: List<PsiNamedElement>
        get() =
            element.importDeclaration?.importedModule?.exportedNewTypeDeclarations
                ?: emptyList()

}
