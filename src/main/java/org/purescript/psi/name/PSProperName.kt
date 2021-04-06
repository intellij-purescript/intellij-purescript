package org.purescript.psi.name

import com.intellij.lang.ASTNode
import org.purescript.psi.PSPsiElement

class PSProperName(node: ASTNode) : PSPsiElement(node) {
    override fun getName(): String {
        return text.trim()
    }
}
