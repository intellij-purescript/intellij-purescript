package org.purescript.psi

import com.intellij.lang.ASTNode

class PSIdentifierImpl(node: ASTNode) : PSPsiElement(node) {
    override fun getName(): String = text
}