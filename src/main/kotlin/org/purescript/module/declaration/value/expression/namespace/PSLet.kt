package org.purescript.module.declaration.value.expression.namespace

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.module.declaration.value.ValueDeclarationGroup
import org.purescript.module.declaration.value.binder.Binder
import org.purescript.module.declaration.value.expression.Expression
import org.purescript.module.declaration.value.expression.ExpressionAtom
import org.purescript.module.declaration.value.expression.ExpressionContext
import org.purescript.module.declaration.value.expression.PSValue
import org.purescript.psi.PSPsiElement

class PSLet(node: ASTNode) : PSPsiElement(node), Expression, ExpressionContext {
    val valueDeclarationGroups get() =
        findChildrenByClass(ValueDeclarationGroup::class.java).asSequence()
    val value: PSValue? = findChildByClass(PSValue::class.java)
    override fun getAtoms(): Sequence<ExpressionAtom> =
        (value?.getAtoms() ?: emptySequence()) + valueDeclarationGroups.flatMap { it.expressionAtoms }

    private val binderChildren = childrenOfType<Binder>().asSequence()
    private val namedBinders = binderChildren.flatMap { it.namedDescendant }
    override val expressionDeclarations get() = valueDeclarationGroups + namedBinders
}