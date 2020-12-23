package net.kenro.ji.jin.purescript.psi.impl

import com.intellij.lang.ASTNode

class PSProperNameImpl(node: ASTNode) : PSPsiElement(node) {
    override fun getName(): String {
        return PSPsiImplUtil.getName(this)
    }
}