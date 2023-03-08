package org.purescript.psi.expression

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.psi.base.PSPsiElement
import org.purescript.psi.binder.BinderAtom
import org.purescript.psi.declaration.value.ValueDeclarationGroup

class PSLet(node: ASTNode) : PSPsiElement(node), Expression {
    val valueDeclarationGroups: Array<ValueDeclarationGroup> get() =
        findChildrenByClass(ValueDeclarationGroup::class.java)
    val value:PSValue? = findChildByClass(PSValue::class.java)
    override fun getAtoms(): Sequence<ExpressionAtom> =
        (value?.getAtoms() ?: emptySequence()) + valueDeclarationGroups.flatMap { it.expressionAtoms }
    val binders: List<BinderAtom> get() = childrenOfType<BinderAtom>().flatMap { it.binders }
}