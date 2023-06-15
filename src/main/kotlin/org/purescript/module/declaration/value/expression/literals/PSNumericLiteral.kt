package org.purescript.module.declaration.value.expression.literals

import com.intellij.lang.ASTNode
import org.purescript.module.declaration.value.Similar
import org.purescript.module.declaration.value.expression.ExpressionAtom
import org.purescript.psi.PSPsiElement
import org.purescript.typechecker.Prim

class PSNumericLiteral(node: ASTNode) : PSPsiElement(node), ExpressionAtom {
    override fun areSimilarTo(other: Similar): Boolean = 
        other is PSNumericLiteral && other.text == this.text
    override fun checkReferenceType() = Prim.int
    override fun checkUsageType() = Prim.int
    override fun checkType() = Prim.int
}