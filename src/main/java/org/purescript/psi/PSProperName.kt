package org.purescript.psi

import com.intellij.lang.ASTNode

class PSProperName(node: ASTNode) : PSPsiElement(node) {
    override fun getName(): String {
        return text.trim()
    }
}