package net.kenro.ji.jin.purescript.psi.impl

import net.kenro.ji.jin.purescript.psi.DeclaresIdentifiers
import net.kenro.ji.jin.purescript.psi.ContainsIdentifier
import com.intellij.psi.PsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiNameIdentifierOwner

class PSValueDeclarationImpl(node: ASTNode) : PSPsiElement(node),
    PsiNameIdentifierOwner {
    override fun getName(): String? {
        return findChildByClass(PSIdentifierImpl::class.java)!!
            .name
    }

    override fun setName(name: String): PsiElement? {
        return null
    }

    override fun getNameIdentifier(): PsiElement? {
        return findChildByClass(PSIdentifierImpl::class.java)
    }

    val declaredIdentifiersInParameterList: Map<String?, PSIdentifierImpl?>
        get() {
            val identifiers = findChildrenByClass(PSIdentifierImpl::class.java)
                .asSequence()
                .drop(1)
                .map(ContainsIdentifier::identifiers)
            val childrenIdentifiers =
                findChildrenByClass(DeclaresIdentifiers::class.java)
                    .asSequence()
                    .map { it.getDeclaredIdentifiers() }
            return (identifiers + childrenIdentifiers)
                .flatMap { it.asSequence() }
                .map { Pair(it.key, it.value) }
                .toMap()
        }
}