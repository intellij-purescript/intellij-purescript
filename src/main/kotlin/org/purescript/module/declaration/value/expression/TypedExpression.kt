package org.purescript.module.declaration.value.expression

import com.intellij.lang.ASTNode
import org.purescript.inference.inferType
import org.purescript.module.declaration.type.type.PSType
import org.purescript.psi.PSPsiElement

class TypedExpression(node: ASTNode) : PSPsiElement(node), ExpressionAtom {
    val value get() = findChildByClass(Expression::class.java)
    val type get() = findChildByClass(PSType::class.java)
    override fun unify() {
        value?.inferType()?.let { unify(it) }
        type?.inferType()?.let { unify(it) }
    }
}