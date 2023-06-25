package org.purescript.module.declaration.value.expression.controll.ifthenelse

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.inference.InferType
import org.purescript.inference.inferType
import org.purescript.module.declaration.value.expression.Expression
import org.purescript.psi.PSPsiElement

class IfThenElse(node: ASTNode) : PSPsiElement(node), IfThenElseAlternative, Expression {
    val ifThenAndElse get() = childrenOfType<Expression>()
    override fun unify() {
        val (ifType, thenType, elseType) = ifThenAndElse.map { it.inferType() }
        unify(ifType, InferType.Boolean)
        unify(thenType)
        unify(elseType)
    }
}