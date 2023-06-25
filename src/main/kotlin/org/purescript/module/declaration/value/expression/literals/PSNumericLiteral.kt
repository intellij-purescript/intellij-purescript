package org.purescript.module.declaration.value.expression.literals

import com.intellij.lang.ASTNode
import org.purescript.inference.InferType
import org.purescript.module.declaration.value.Similar
import org.purescript.module.declaration.value.expression.ExpressionAtom
import org.purescript.psi.PSPsiElement

class PSNumericLiteral(node: ASTNode) : PSPsiElement(node), ExpressionAtom {
    override fun areSimilarTo(other: Similar): Boolean =
        other is PSNumericLiteral && other.text == this.text

    override val substitutedType: InferType
        get() = if (text.contains(".")) InferType.Number else InferType.Int
}