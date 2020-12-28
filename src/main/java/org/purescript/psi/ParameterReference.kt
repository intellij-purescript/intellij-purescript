package org.purescript.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.util.parentOfType
import org.jetbrains.annotations.NotNull

class ParameterReference (element: @NotNull PSPsiElement) : PsiReferenceBase<PSPsiElement?>(
    element,
    TextRange.allOf(element.text.trim())
) {

    override fun resolve(): PSIdentifierImpl? {
        return element
            .parentOfType<PSValueDeclaration>(false)
            ?.declaredIdentifiersInParameterList
            ?.get(element.text.trim())
    }

    override fun getVariants(): Array<PSIdentifierImpl?> {
        return element
            .parentOfType<PSValueDeclaration>(false)
            ?.declaredIdentifiersInParameterList
            ?.values
            ?.toTypedArray()
            ?: emptyArray()
    }

}