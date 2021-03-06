package org.purescript.psi

import com.intellij.lang.ASTNode

class PSIdentifier(node: ASTNode) : PSPsiElement(node) {
    override fun getName(): String = text
}