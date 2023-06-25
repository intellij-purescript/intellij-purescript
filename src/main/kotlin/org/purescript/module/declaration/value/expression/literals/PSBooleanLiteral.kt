package org.purescript.module.declaration.value.expression.literals

import com.intellij.lang.ASTNode
import org.purescript.inference.InferType
import org.purescript.module.declaration.value.Similar
import org.purescript.module.declaration.value.expression.ExpressionAtom
import org.purescript.psi.PSPsiElement

class PSBooleanLiteral(node: ASTNode) : PSPsiElement(node), ExpressionAtom {
    override fun areSimilarTo(other: Similar): Boolean = other is PSBooleanLiteral && other.text == this.text
    override fun unify() = unify(InferType.Boolean)
}