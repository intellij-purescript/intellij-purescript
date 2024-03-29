package org.purescript.name

import com.intellij.lang.ASTNode
import org.purescript.psi.PSPsiElement

class PSClassName(node: ASTNode) : PSPsiElement(node) {
    override fun getName(): String = text
}
