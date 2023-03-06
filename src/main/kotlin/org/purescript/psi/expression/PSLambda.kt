package org.purescript.psi.expression

import com.intellij.lang.ASTNode
import org.purescript.psi.base.PSPsiElement
import org.purescript.psi.binder.Parameters

class PSLambda(node: ASTNode) : PSPsiElement(node), Expression {
    val binders get() = parameters?.binders ?: emptyList()
    val parameters get() = findChildByClass(Parameters::class.java)
    val value: PSValue? get() = findChildByClass(PSValue::class.java)
}