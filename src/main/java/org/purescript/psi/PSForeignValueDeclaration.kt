package org.purescript.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner

class PSForeignValueDeclaration(node: ASTNode) : PSPsiElement(node),
    PsiNameIdentifierOwner {
    override fun setName(name: String): PsiElement? {
        return null
    }

    override fun getNameIdentifier(): PSIdentifierImpl? {
        return findChildByClass(PSIdentifierImpl::class.java)
    }

    override fun getName(): String? {
        return nameIdentifier?.name
    }
}