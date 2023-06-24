package org.purescript.module.declaration.value.expression.literals

import com.intellij.lang.ASTNode
import org.purescript.inference.InferType
import org.purescript.inference.Scope
import org.purescript.module.declaration.value.Similar
import org.purescript.module.declaration.value.expression.ExpressionAtom
import org.purescript.psi.PSPsiElement

class PSStringLiteral(node: ASTNode) : 
    PSPsiElement(node), 
    ExpressionAtom {
    override fun areSimilarTo(other: Similar): Boolean =
        other is PSStringLiteral && this.text == other.text
    override fun infer(scope: Scope): InferType = InferType.String
    override fun unify() = unify(InferType.String)
}