package org.purescript.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner

class PSVarBinderImpl(node: ASTNode) :
    PSPsiElement(node), PsiNameIdentifierOwner, DeclaresIdentifiers {
    override fun getDeclaredIdentifiers(): Map<String?, PSIdentifierImpl?> {
        return findChildrenByClass(ContainsIdentifier::class.java)
            .asSequence()
            .map { it.identifiers }
            .flatMap { it.asSequence() }
            .map { Pair(it.key, it.value) }
            .toMap()
    }

    override fun getName(): String = nameIdentifier.name

    override fun setName(name: String): PsiElement? {
        return null
    }

    override fun getNameIdentifier(): PSIdentifierImpl {
        return findChildByClass(PSIdentifierImpl::class.java)!!
    }
}