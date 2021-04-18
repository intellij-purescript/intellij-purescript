package org.purescript.psi

import com.intellij.lang.ASTNode

class PSDoNotationLetImpl(node: ASTNode) : PSPsiElement(node) {
    val valueDeclarations get() =
        findChildrenByClass(PSValueDeclaration::class.java)
}