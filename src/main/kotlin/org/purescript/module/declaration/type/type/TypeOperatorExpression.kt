package org.purescript.module.declaration.type.type

import com.intellij.lang.ASTNode
import org.purescript.inference.InferType
import org.purescript.inference.Inferable
import org.purescript.psi.PSPsiElement

class TypeOperatorExpression(node: ASTNode) : PSPsiElement(node), PSType, Inferable {
    private val types get() = findChildrenByClass(PSType::class.java)
    private val operator get() = findChildrenByClass(TypeOperator::class.java).first()

    override fun unify() {
        return
        val (first, second) = types
        val leftHand = first.inferType()
        val rightHand = second.inferType()
        val operatorType = operator.inferType()
        val ret = module.newId()
        unify(operatorType, InferType.function(leftHand, ret))
        val app1 = module.substitute(ret)
        val ret1 = module.newId()
        unify(app1, InferType.function(rightHand, ret1))
        val app2 = module.substitute(ret1)
        unify(app2)
    }

}