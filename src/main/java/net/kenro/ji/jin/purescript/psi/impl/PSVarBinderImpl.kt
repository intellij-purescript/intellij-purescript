package net.kenro.ji.jin.purescript.psi.impl

import com.intellij.lang.ASTNode
import net.kenro.ji.jin.purescript.psi.ContainsIdentifier
import net.kenro.ji.jin.purescript.psi.DeclaresIdentifiers

class PSVarBinderImpl(node: ASTNode) : PSPsiElement(node), DeclaresIdentifiers {
    override fun getDeclaredIdentifiers(): Map<String?, PSIdentifierImpl?> {
        return findChildrenByClass(ContainsIdentifier::class.java)
            .asSequence()
            .map(ContainsIdentifier::identifiers)
            .flatMap { it.asSequence() }
            .map { Pair(it.key, it.value) }
            .toMap()
    }
}