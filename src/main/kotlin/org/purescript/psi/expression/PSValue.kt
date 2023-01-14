package org.purescript.psi.expression

import com.intellij.lang.ASTNode
import org.purescript.psi.base.PSPsiElement
import org.purescript.psi.expression.ExpressionAtom

class PSValue(node: ASTNode) : PSPsiElement(node) {
    val expressionAtoms
        get() = findChildrenByClass(ExpressionAtom::class.java).toList()
}