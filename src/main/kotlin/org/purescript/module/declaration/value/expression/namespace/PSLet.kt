package org.purescript.module.declaration.value.expression.namespace

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.psi.PSPsiElement
import org.purescript.module.declaration.value.binder.Binder
import org.purescript.module.declaration.value.ValueDeclarationGroup
import org.purescript.module.declaration.value.expression.ExpressionAtom
import org.purescript.module.declaration.value.expression.PSValue

class PSLet(node: ASTNode) : PSPsiElement(node), org.purescript.module.declaration.value.expression.Expression {
    val valueDeclarationGroups: Array<ValueDeclarationGroup> get() =
        findChildrenByClass(ValueDeclarationGroup::class.java)
    val value: PSValue? = findChildByClass(PSValue::class.java)
    override fun getAtoms(): Sequence<ExpressionAtom> =
        (value?.getAtoms() ?: emptySequence()) + valueDeclarationGroups.flatMap { it.expressionAtoms }
    val binders: List<Binder> get() = childrenOfType<Binder>().flatMap { it.descendantBinders }
}