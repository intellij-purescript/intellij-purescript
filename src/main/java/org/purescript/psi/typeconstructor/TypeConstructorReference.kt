package org.purescript.psi.typeconstructor

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase

class TypeConstructorReference(typeConstructor: PSTypeConstructor) :
    PsiReferenceBase<PSTypeConstructor>(
        typeConstructor,
        typeConstructor.textRangeInParent,
        false
    ) {
    override fun resolve(): PsiElement? {
        return myElement.module.dataDeclarations.firstOrNull {it.name == myElement.name}
    }
}
