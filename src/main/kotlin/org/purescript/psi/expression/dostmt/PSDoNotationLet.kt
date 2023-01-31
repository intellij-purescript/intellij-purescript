package org.purescript.psi.expression.dostmt

import com.intellij.lang.ASTNode
import org.purescript.psi.base.PSPsiElement
import org.purescript.psi.declaration.value.ValueDeclarationGroup

class PSDoNotationLet(node: ASTNode) : PSPsiElement(node) {
    val valueDeclarationGroups: Array<ValueDeclarationGroup>
        get() =
        findChildrenByClass(ValueDeclarationGroup::class.java)
}