package org.purescript.module.declaration.value.expression.literals

import com.intellij.lang.ASTNode
import org.purescript.inference.Scope
import org.purescript.inference.Type
import org.purescript.module.declaration.value.Similar
import org.purescript.module.declaration.value.expression.ExpressionAtom
import org.purescript.psi.PSPsiElement
import org.purescript.typechecker.Prim

class PSBooleanLiteral(node: ASTNode) : PSPsiElement(node), ExpressionAtom {
    override fun checkType() = Prim.boolean
    override fun checkReferenceType() = Prim.boolean
    override fun infer(scope: Scope): Type = Type.Boolean
    override fun checkUsageType() = Prim.boolean
    override fun areSimilarTo(other: Similar): Boolean =
        other is PSBooleanLiteral && other.text == this.text
}