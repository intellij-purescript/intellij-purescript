package org.purescript.psi

import com.intellij.lang.ASTNode

class PSLetImpl(node: ASTNode) : PSPsiElement(node) {
    val valueDeclarations: Array<PSValueDeclaration> get() =
        findChildrenByClass(PSValueDeclaration::class.java)
}