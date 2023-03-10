package org.purescript.psi.expression.literals

import com.intellij.lang.ASTNode
import org.purescript.psi.base.PSPsiElement
import org.purescript.psi.expression.Expression
import org.purescript.psi.expression.ExpressionAtom

class PSNumericLiteral(node: ASTNode) : PSPsiElement(node), ExpressionAtom {
    override fun areSimilarTo(other: Expression): Boolean = 
        other is PSNumericLiteral && other.text == this.text
}