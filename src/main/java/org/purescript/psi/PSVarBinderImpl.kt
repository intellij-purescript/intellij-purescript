package org.purescript.psi

import com.intellij.lang.ASTNode

class PSVarBinderImpl(node: ASTNode) : PSPsiElement(node), DeclaresIdentifiers {
    override fun getDeclaredIdentifiers(): Map<String?, PSIdentifierImpl?> {
        return findChildrenByClass(ContainsIdentifier::class.java)
            .asSequence()
            .map { it.identifiers }
            .flatMap { it.asSequence() }
            .map { Pair(it.key, it.value) }
            .toMap()
    }
}