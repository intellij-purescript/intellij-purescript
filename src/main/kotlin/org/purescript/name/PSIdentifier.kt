package org.purescript.name

import com.intellij.lang.ASTNode
import org.purescript.psi.PSPsiElement

class PSIdentifier(node: ASTNode) : PSPsiElement(node) {
    override fun getName(): String = text
    fun nameMatches(name: String) = textMatches(name)
}
