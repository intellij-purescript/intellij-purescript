package org.purescript.module.declaration.value.expression.identifier

import com.intellij.lang.ASTNode
import org.purescript.inference.inferType
import org.purescript.module.declaration.value.expression.Expression
import org.purescript.module.declaration.value.expression.ExpressionAtom
import org.purescript.psi.PSPsiElement

class Argument(node: ASTNode) : PSPsiElement(node), Expression {
    val arguments: Sequence<ExpressionAtom> get() = emptySequence()
    val value get() = findChildByClass(Expression::class.java) ?: error("empty argument")
    val call get() = parent as? Call
    override fun unify() = unify(value.inferType())
}