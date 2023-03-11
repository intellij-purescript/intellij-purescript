package org.purescript.module.declaration.value.expression.namespace

import com.intellij.lang.ASTNode
import org.purescript.module.declaration.value.expression.Expression
import org.purescript.module.declaration.value.ValueNamespace
import org.purescript.module.declaration.value.expression.PSValue
import org.purescript.module.declaration.value.parameters.Parameters
import org.purescript.psi.PSPsiElement

class PSLambda(node: ASTNode) : PSPsiElement(node), Expression, ValueNamespace {
    val namedDescendant get() = parameterList?.namedDescendant ?: emptyList()
    val parameterList get() = findChildByClass(Parameters::class.java)
    val parameters get() = parameterList?.parameters
    val value: PSValue? get() = findChildByClass(PSValue::class.java)
    override val valueNames get() = namedDescendant.asSequence()
}