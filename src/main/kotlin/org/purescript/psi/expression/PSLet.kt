package org.purescript.psi.expression

import com.intellij.lang.ASTNode
import org.purescript.psi.base.PSPsiElement
import org.purescript.psi.declaration.value.ValueDecl

class PSLet(node: ASTNode) : PSPsiElement(node) {
    val valueDeclarations: Array<ValueDecl> get() =
        findChildrenByClass(ValueDecl::class.java)
}