package org.purescript.psi.declaration

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import org.purescript.psi.PSPsiFactory

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

    override fun handleElementRename(name: String): PsiElement? {
        val oldName = element.identifier
        val newName = PSPsiFactory(element.project).createIdentifier(name)
            ?: return null
        oldName.replace(newName)
        return element
    }
}
