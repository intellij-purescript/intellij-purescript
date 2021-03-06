package org.purescript.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner

class PSVarBinderImpl(node: ASTNode) :
    PSPsiElement(node), PsiNameIdentifierOwner {

    override fun getName(): String = nameIdentifier.name

    override fun setName(name: String): PsiElement? {
        return null
    }

    override fun getNameIdentifier(): PSIdentifier {
        return findChildByClass(PSIdentifier::class.java)!!
    }
}