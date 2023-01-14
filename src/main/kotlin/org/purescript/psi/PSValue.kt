package org.purescript.psi

import com.intellij.lang.ASTNode
import org.purescript.psi.base.PSPsiElement
import org.purescript.psi.expression.ExpressionAtom
import org.purescript.psi.expression.PSExpressionIdentifier
import org.purescript.psi.expression.PSExpressionOperator

class PSValue(node: ASTNode) : PSPsiElement(node) {
    val expressionAtoms
        get() = findChildrenByClass(ExpressionAtom::class.java).toList()
    val expressionOperators 
        get() = findChildrenByClass(PSExpressionOperator::class.java).toList()
    val expressionIdentifiers: List<PSExpressionIdentifier>
        get() = findChildrenByClass(PSExpressionIdentifier::class.java).toList()
}