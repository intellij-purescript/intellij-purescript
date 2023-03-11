package org.purescript.module.declaration.value.expression.dostmt

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.module.declaration.value.ValueDeclarationGroup
import org.purescript.module.declaration.value.binder.Binder
import org.purescript.psi.PSPsiElement

class PSDoNotationLet(node: ASTNode) : PSPsiElement(node), DoStatement {
    val valueDeclarationGroups: Array<ValueDeclarationGroup>
        get() = findChildrenByClass(ValueDeclarationGroup::class.java)
    override val binders get() = childrenOfType<Binder>().asSequence().flatMap { it.descendantBinders }
    override val valueNames get() = super.valueNames + valueDeclarationGroups
}