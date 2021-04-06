package org.purescript.psi.name

import com.intellij.lang.ASTNode
import org.purescript.psi.PSPsiElement

class PSModuleName(node: ASTNode) : PSPsiElement(node) {
    override fun getName(): String = text.trimEnd('.')
}
