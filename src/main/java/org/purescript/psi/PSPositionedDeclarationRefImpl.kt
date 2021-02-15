package org.purescript.psi

import com.intellij.lang.ASTNode

class PSPositionedDeclarationRefImpl(node: ASTNode) : PSPsiElement(node) {
    val isModuleExport: Boolean get() =
        firstChild
            ?.firstChild
            ?.textMatches("module")
            ?: false
}