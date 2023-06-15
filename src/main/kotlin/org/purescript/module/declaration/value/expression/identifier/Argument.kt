package org.purescript.module.declaration.value.expression.identifier

import com.intellij.lang.ASTNode
import org.purescript.module.declaration.value.expression.Expression
import org.purescript.module.declaration.value.expression.ExpressionAtom
import org.purescript.psi.PSPsiElement

class Argument(node: ASTNode) : PSPsiElement(node), Expression {
    val arguments: Sequence<ExpressionAtom> get() = emptySequence()
    val value get() = findChildByClass(Expression::class.java)
    val call get() = parent as? Call
    override fun checkUsageType() = call?.function?.checkType()?.argument
    override fun checkReferenceType() = value?.checkReferenceType()
}