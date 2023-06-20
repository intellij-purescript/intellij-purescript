package org.purescript.module.declaration.value.expression.controll.ifthenelse

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.inference.Scope
import org.purescript.inference.Type
import org.purescript.module.declaration.value.expression.Expression
import org.purescript.psi.PSPsiElement

class ErrorIf(node: ASTNode) : PSPsiElement(node), IfThenElseAlternative, Expression {
    val expr get() = childrenOfType<Expression>().single()
    override fun infer(scope: Scope): Type {
        scope.unify(Type.Boolean, expr.infer(scope))
        return scope.newUnknown()
    }
}