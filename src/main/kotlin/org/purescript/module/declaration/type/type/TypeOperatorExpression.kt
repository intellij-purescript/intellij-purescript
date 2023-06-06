package org.purescript.module.declaration.type.type

import com.intellij.lang.ASTNode
import org.purescript.psi.PSPsiElement
import org.purescript.typechecker.TypeCheckable
import org.purescript.typechecker.TypeCheckerType

class TypeOperatorExpression(node: ASTNode) : PSPsiElement(node), PSType {
    private val types get() = findChildrenByClass(PSType::class.java)
    override fun checkType(): TypeCheckerType? {
        val types = types
        if (types.size != 2) return null
        val (first, second) = types
        val firstType = first.checkType() ?: return null
        val secondType = second.checkType() ?: return null
        val operatorType = (reference?.resolve() as? TypeCheckable)?.checkType() ?: return null
        return TypeCheckerType.TypeApp(
            TypeCheckerType.TypeApp(operatorType, firstType),
            secondType
        )
    }
}