package org.purescript.module.declaration.value.expression.namespace

import com.intellij.lang.ASTNode
import org.purescript.psi.PSPsiElement
import org.purescript.module.declaration.value.expression.PSValue
import org.purescript.module.declaration.value.parameters.Parameters

class PSLambda(node: ASTNode) : PSPsiElement(node), org.purescript.module.declaration.value.expression.Expression {
    val namedDescendant get() = parameterList?.namedDescendant ?: emptyList()
    val parameterList get() = findChildByClass(Parameters::class.java)
    val parameters get() = parameterList?.parameters
    val value: PSValue? get() = findChildByClass(PSValue::class.java)
}