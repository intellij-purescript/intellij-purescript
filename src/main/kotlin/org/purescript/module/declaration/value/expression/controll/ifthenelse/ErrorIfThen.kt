package org.purescript.module.declaration.value.expression.controll.ifthenelse

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.inference.InferType

import org.purescript.module.declaration.value.expression.Expression
import org.purescript.psi.PSPsiElement

class ErrorIfThen(node: ASTNode) : PSPsiElement(node), IfThenElseAlternative, Expression {
    val ifThen get() = childrenOfType<Expression>()
    override fun unify() {
        val (ifType, thenType) = ifThen.map { it.inferType() }
        unify(InferType.Boolean, ifType)
        unify(thenType)
    }
}