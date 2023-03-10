package org.purescript.module.declaration.value.expression.identifier

import com.intellij.lang.ASTNode
import org.purescript.psi.PSPsiElement
import org.purescript.module.declaration.value.expression.ExpressionAtom

class Argument(node: ASTNode) : PSPsiElement(node), org.purescript.module.declaration.value.expression.Expression {
    val arguments: Sequence<ExpressionAtom> get() = emptySequence()
}