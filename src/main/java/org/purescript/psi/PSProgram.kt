package org.purescript.psi

import com.intellij.lang.ASTNode

class PSProgram(node: ASTNode) : PSPsiElement(node) {
    val module: PSModule
        get() = this.firstChild as PSModule
}