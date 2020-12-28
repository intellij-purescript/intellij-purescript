package org.purescript.psi

import com.intellij.lang.ASTNode

class PSProperNameImpl(node: ASTNode) : PSPsiElement(node) {
    override fun getName(): String {
        return text.trim()
    }
}