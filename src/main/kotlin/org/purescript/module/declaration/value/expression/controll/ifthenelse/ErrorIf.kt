package org.purescript.module.declaration.value.expression.controll.ifthenelse

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.inference.InferType
import org.purescript.inference.inferType
import org.purescript.module.declaration.value.expression.Expression
import org.purescript.psi.PSPsiElement

class ErrorIf(node: ASTNode) : PSPsiElement(node), IfThenElseAlternative, Expression {
    val expr get() = childrenOfType<Expression>().single()
    override fun unify() {
        unify(InferType.Boolean, expr.inferType())
    }
}