package org.purescript.psi.expression

import com.intellij.lang.ASTNode
import org.purescript.psi.base.PSPsiElement
import org.purescript.psi.declaration.value.ValueDeclarationGroup

class PSLet(node: ASTNode) : PSPsiElement(node) {
    val valueDeclarationGroups: Array<ValueDeclarationGroup> get() =
        findChildrenByClass(ValueDeclarationGroup::class.java)
}