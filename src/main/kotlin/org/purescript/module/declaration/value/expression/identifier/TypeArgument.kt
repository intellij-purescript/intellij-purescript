package org.purescript.module.declaration.value.expression.identifier

import com.intellij.lang.ASTNode
import org.purescript.module.declaration.value.expression.Expression
import org.purescript.module.declaration.value.expression.ExpressionAtom
import org.purescript.psi.PSPsiElement

class TypeArgument(node: ASTNode) : PSPsiElement(node), Expression {
    val arguments: Sequence<ExpressionAtom> get() = emptySequence()
}