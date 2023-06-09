package org.purescript.module.declaration.value.expression

import com.intellij.lang.ASTNode
import org.purescript.module.declaration.type.type.PSType
import org.purescript.psi.PSPsiElement
import org.purescript.typechecker.TypeCheckerType

class TypedExpression(node: ASTNode) : PSPsiElement(node), ExpressionAtom {
    val value get() = findChildByClass(Expression::class.java)
    val type get() = findChildByClass(PSType::class.java)
    override fun checkType(): TypeCheckerType? = 
        type?.checkType() ?: value?.checkType()
}