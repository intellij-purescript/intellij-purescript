package org.purescript.module.declaration.value.expression.dostmt

import com.intellij.lang.ASTNode
import org.purescript.module.declaration.value.expression.Expression
import org.purescript.psi.PSPsiElement

class EmptyDoBlock(node: ASTNode) : PSPsiElement(node), Expression {
    override val expressions get() = emptySequence<Expression>()
}