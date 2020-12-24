package net.kenro.ji.jin.purescript.psi

import com.intellij.lang.ASTNode

class PSConstructorBinderImpl(node: ASTNode) : PSPsiElement(node), DeclaresIdentifiers {
    override fun getDeclaredIdentifiers(): Map<String?, PSIdentifierImpl?> {
        val identifiers =
            findChildrenByClass(PSIdentifierImpl::class.java)
            .asSequence()
            .drop(1)
            .map(ContainsIdentifier::identifiers)

        val childrenIdentifiers =
            findChildrenByClass(
                DeclaresIdentifiers::class.java
            )
            .asSequence()
            .map { it.getDeclaredIdentifiers() }
        return (identifiers + childrenIdentifiers)
            .flatMap { it.asSequence() }
            .map { Pair(it.key, it.value) }
            .toMap()
    }
}