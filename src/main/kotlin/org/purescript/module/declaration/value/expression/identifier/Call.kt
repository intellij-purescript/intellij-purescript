package org.purescript.module.declaration.value.expression.identifier

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.inference.InferType
import org.purescript.inference.Scope
import org.purescript.inference.unifyAndSubstitute
import org.purescript.module.declaration.value.expression.Expression
import org.purescript.psi.PSPsiElement

class Call(node: ASTNode) : PSPsiElement(node), Expression {
    val arguments: Sequence<Argument>
        get() =
            childrenOfType<Argument>().asSequence() +
                    ((parent as? Call)?.arguments ?: emptySequence())
    val function get() = findChildByClass(Expression::class.java)
    val argument get() = findChildByClass(Argument::class.java)
    override fun infer(scope: Scope) = scope.inferApp(
        function!!.infer(scope),
        argument!!.infer(scope)
    )

    override fun unify() {
        val functionType = function?.unifyAndSubstitute() ?: return
        val argumentType = argument?.unifyAndSubstitute() ?: return 
        val returnType = substitutedType
        val callType = InferType.function(argumentType, returnType)
        unify(functionType, callType)
    }
}