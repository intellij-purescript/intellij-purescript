package net.kenro.ji.jin.purescript.psi.impl

import net.kenro.ji.jin.purescript.psi.DeclaresIdentifiers
import net.kenro.ji.jin.purescript.psi.ContainsIdentifier
import com.intellij.lang.ASTNode
import java.util.*
import kotlin.streams.asSequence

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