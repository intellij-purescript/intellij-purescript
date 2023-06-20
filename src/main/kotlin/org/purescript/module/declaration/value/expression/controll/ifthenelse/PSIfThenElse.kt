package org.purescript.module.declaration.value.expression.controll.ifthenelse

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.inference.Scope
import org.purescript.inference.Type
import org.purescript.module.declaration.value.expression.Expression
import org.purescript.psi.PSPsiElement

class PSIfThenElse(node: ASTNode) : PSPsiElement(node), Expression {
    val ifThenAndElse get() = childrenOfType<Expression>()
    override fun infer(scope: Scope): Type {
        val types = ifThenAndElse.map { it.infer(scope) }
        val ifType = types.getOrElse(0) { scope.newUnknown() }
        val thenType = types.getOrElse(1) { scope.newUnknown() }
        val elseType = types.getOrElse(2) { scope.newUnknown() }
        scope.unify(Type.Boolean, ifType)
        scope.unify(thenType, elseType)
        return thenType
    }
}