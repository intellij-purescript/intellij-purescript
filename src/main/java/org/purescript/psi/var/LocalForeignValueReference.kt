package org.purescript.psi.`var`

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiReferenceBase
import org.purescript.psi.PSForeignValueDeclaration

class LocalForeignValueReference(element: PSVar) : PsiReferenceBase<PSVar>(
    element,
    TextRange.allOf(element.text.trim()),
    false
) {

    override fun getVariants(): Array<PSForeignValueDeclaration> {
        return myElement.module?.foreignValueDeclarations
            ?: emptyArray()
    }

    override fun resolve(): PSForeignValueDeclaration? {
        return variants.find { it.name == myElement.name }
    }

}
