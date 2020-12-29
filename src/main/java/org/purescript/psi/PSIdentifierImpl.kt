package org.purescript.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement

// PsiNamedElement is only here so that the editor can find the the
// Identifier when it is in a parameter
class PSIdentifierImpl(node: ASTNode) : PSPsiElement(node), ContainsIdentifier,
    PsiNamedElement {
    override fun getName(): String {
        return text
    }

    override fun setName(name: String): PsiElement? {
        return null
    }

    override val identifiers
        get() = mapOf<String?, PSIdentifierImpl>(Pair(this.name, this))
}