package org.purescript.module.declaration.value.expression.identifier

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.inference.Scope
import org.purescript.inference.Type
import org.purescript.module.declaration.value.expression.Expression
import org.purescript.psi.PSPsiElement
import org.purescript.typechecker.TypeCheckerType

class Call(node: ASTNode) : PSPsiElement(node), Expression {
    val arguments: Sequence<Argument> get() = 
        childrenOfType<Argument>().asSequence() + 
            ((parent as? Call)?.arguments ?: emptySequence())
    val function get() = findChildByClass(Expression::class.java)
    val argument get() = findChildByClass(Argument::class.java)
    
    override fun checkReferenceType(): TypeCheckerType? {
        return when (val functionType = function?.checkType()) {
            is TypeCheckerType.TypeApp -> functionType.to
            is TypeCheckerType.ForAll -> argument?.checkType()?.let { functionType.call(it) }
            else -> null
        }
    }

    override fun infer(scope: Scope): Type {
        val argumentType = argument!!.infer(scope)
        val functionType = function!!.infer(scope)
        val returnType = scope.newUnknown()
        scope.unify(functionType, Type.function(argumentType, returnType))
        return returnType
    }

    override fun checkUsageType(): TypeCheckerType? = null
}