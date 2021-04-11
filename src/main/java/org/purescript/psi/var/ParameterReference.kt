package org.purescript.psi.`var`

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.util.parentOfType
import org.jetbrains.annotations.NotNull
import org.purescript.psi.PSPsiElement
import org.purescript.psi.PSValueDeclaration
import org.purescript.psi.PSVarBinderImpl

class ParameterReference (element: PSPsiElement) : PsiReferenceBase<PSPsiElement>(
    element,
    TextRange.allOf(element.text.trim())
) {

    override fun resolve(): PSVarBinderImpl? {
        return element
            .parentOfType<PSValueDeclaration>(false)
            ?.varBindersInParameters
            ?.get(element.text.trim())
    }

    override fun getVariants(): Array<PSVarBinderImpl?> {
        return element
            .parentOfType<PSValueDeclaration>(false)
            ?.varBindersInParameters
            ?.values
            ?.toTypedArray()
            ?: emptyArray()
    }

}
