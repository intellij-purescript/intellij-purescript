package org.purescript.module.declaration.value.expression.namespace

import com.intellij.lang.ASTNode
import org.purescript.inference.InferType
import org.purescript.module.declaration.value.ValueNamespace
import org.purescript.module.declaration.value.expression.Expression
import org.purescript.module.declaration.value.parameters.Parameters
import org.purescript.psi.PSPsiElement

class PSLambda(node: ASTNode) : PSPsiElement(node), Expression, ValueNamespace {
    val parameterList get() = findChildByClass(Parameters::class.java)
    val parameters get() = parameterList?.parameters
    val value get() = findChildByClass(Expression::class.java)
    override val valueNames get() = parameterList?.valueNames ?: emptySequence()
    override fun unify() {
        val parameters = parameters ?: return
        val ret = value ?: return
        unify(InferType.function(
            *parameters.map { it.inferType() }.toTypedArray(),
            ret.inferType()
        ))
    }
}