package org.purescript.psi

import com.intellij.lang.ASTNode

class PSIdentifierImpl(node: ASTNode) : PSPsiElement(node), ContainsIdentifier {
    override fun getName(): String = text

    override val identifiers
        get() = mapOf<String?, PSIdentifierImpl>(Pair(this.name, this))
}