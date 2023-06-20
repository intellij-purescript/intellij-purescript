package org.purescript.module.declaration.value.expression

import com.intellij.lang.ASTNode
import org.purescript.inference.Inferable
import org.purescript.inference.Scope
import org.purescript.inference.Type
import org.purescript.module.declaration.type.type.PSType
import org.purescript.psi.PSPsiElement
import org.purescript.typechecker.TypeCheckerType

class TypedExpression(node: ASTNode) : PSPsiElement(node), ExpressionAtom {
    val value get() = findChildByClass(Expression::class.java)
    val type get() = findChildByClass(PSType::class.java)
    override fun checkType(): TypeCheckerType? = 
        type?.checkType() ?: value?.checkType()

    override fun infer(scope: Scope): Type {
        val valueType = value!!.infer(scope)
        val typeType = (type as Inferable).infer(scope)
        scope.unify(valueType, typeType)
        return typeType
    }
}