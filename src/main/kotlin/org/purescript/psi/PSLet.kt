package org.purescript.psi

import com.intellij.lang.ASTNode
import org.purescript.psi.base.PSPsiElement
import org.purescript.psi.declaration.value.PSValueDeclaration

class PSLet(node: ASTNode) : PSPsiElement(node) {
    val valueDeclarations: Array<PSValueDeclaration> get() =
        findChildrenByClass(PSValueDeclaration::class.java)
}