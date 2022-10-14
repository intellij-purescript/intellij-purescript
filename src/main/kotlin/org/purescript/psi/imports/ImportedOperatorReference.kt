package org.purescript.psi.imports

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase

class ImportedOperatorReference(element: PSImportedOperator) :
    PsiReferenceBase<PSImportedOperator>(
        element,
        element.symbol.operator.textRangeInParent,
        false
    ) {

    override fun getVariants(): Array<Any> =
        candidates.toTypedArray()

    override fun resolve(): PsiElement? {
        return candidates.firstOrNull { it.name == element.name }
    }

    val candidates
        get() =
            element
                .importDeclaration
                ?.importedModule
                ?.exportedFixityDeclarations
                ?: listOf()

}
