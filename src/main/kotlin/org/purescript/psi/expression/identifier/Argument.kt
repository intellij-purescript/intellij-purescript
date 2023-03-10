package org.purescript.psi.expression.identifier

import com.intellij.lang.ASTNode
import org.purescript.psi.base.PSPsiElement
import org.purescript.psi.expression.Expression
import org.purescript.psi.expression.ExpressionAtom

class Argument(node: ASTNode) : PSPsiElement(node), Expression {
    val arguments: Sequence<ExpressionAtom> get() = emptySequence()
}