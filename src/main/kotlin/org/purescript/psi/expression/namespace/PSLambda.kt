package org.purescript.psi.expression.namespace

import com.intellij.lang.ASTNode
import org.purescript.psi.base.PSPsiElement
import org.purescript.psi.expression.Expression
import org.purescript.psi.expression.PSValue
import org.purescript.psi.parameters.Parameters

class PSLambda(node: ASTNode) : PSPsiElement(node), Expression {
    val namedDescendant get() = parameterList?.namedDescendant ?: emptyList()
    val parameterList get() = findChildByClass(Parameters::class.java)
    val parameters get() = parameterList?.parameters
    val value: PSValue? get() = findChildByClass(PSValue::class.java)
}