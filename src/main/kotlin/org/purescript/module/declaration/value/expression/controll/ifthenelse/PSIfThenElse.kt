package org.purescript.module.declaration.value.expression.controll.ifthenelse

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.inference.Scope
import org.purescript.inference.Type
import org.purescript.module.declaration.value.expression.Expression
import org.purescript.psi.PSPsiElement
import org.purescript.typechecker.TypeCheckerType

class PSIfThenElse(node: ASTNode) : PSPsiElement(node), Expression {
    override fun checkType(): TypeCheckerType? = null
    val ifThenAndElse get() = childrenOfType<Expression>()
    override fun infer(scope: Scope): Type {
        val (`if`, then, `else`) = ifThenAndElse
        val ifType = `if`.infer(scope)
        val thenType = then.infer(scope)
        val elseType = `else`.infer(scope)
        scope.unify(Type.Boolean, ifType)
        scope.unify(thenType, elseType)
        return thenType
    }
}