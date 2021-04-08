package org.purescript.psi.name

import com.intellij.lang.ASTNode
import org.purescript.psi.PSPsiElement

class PSIdentifier(node: ASTNode) : PSPsiElement(node) {
    override fun getName(): String = text
}
