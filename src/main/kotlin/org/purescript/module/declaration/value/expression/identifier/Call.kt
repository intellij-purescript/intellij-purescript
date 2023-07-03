package org.purescript.module.declaration.value.expression.identifier

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.inference.InferType

import org.purescript.module.declaration.value.expression.Expression
import org.purescript.psi.PSPsiElement

/**
 * f a in expression like 
 * x = f a
 */
class Call(node: ASTNode) : PSPsiElement(node), Expression {
    val arguments: Sequence<Argument>
        get() =
            childrenOfType<Argument>().asSequence() +
                    ((parent as? Call)?.arguments ?: emptySequence())
    val function get() = findChildByClass(Expression::class.java)
    val argument get() = findChildByClass(Argument::class.java)
    override fun unify() {
        val functionType = function?.inferType() ?: return
        val argumentType = argument?.inferType() ?: return 
        val returnType = substitutedType
        val callType = InferType.function(argumentType, returnType)
        unify(functionType, callType)
    }
}