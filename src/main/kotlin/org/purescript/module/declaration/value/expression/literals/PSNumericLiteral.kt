package org.purescript.module.declaration.value.expression.literals

import com.intellij.lang.ASTNode
import org.purescript.module.declaration.value.Similar
import org.purescript.module.declaration.value.expression.ExpressionAtom
import org.purescript.psi.PSPsiElement
import org.purescript.typechecker.TypeCheckerType
import org.purescript.typechecker.TypeConstructor

class PSNumericLiteral(node: ASTNode) : PSPsiElement(node), ExpressionAtom {
    override fun areSimilarTo(other: Similar): Boolean = 
        other is PSNumericLiteral && other.text == this.text

    override fun checkType(): TypeCheckerType = TypeConstructor("Prim.Int")
}