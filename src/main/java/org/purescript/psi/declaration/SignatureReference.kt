package org.purescript.psi.declaration

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase

class SignatureReference(signature: PSSignature) :
    PsiReferenceBase<PSSignature>(
        signature,
        signature.identifier.textRangeInParent,
        false
    ) {
    override fun resolve(): PsiElement? {
        return element.module
            ?.valueDeclarations
            ?.firstOrNull {it.name == element.name}
    }
}
