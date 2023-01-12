package org.purescript.psi

import com.intellij.lang.ASTNode
import org.purescript.psi.base.PSPsiElement
import org.purescript.psi.expression.PSExpressionIdentifier

class PSValue(node: ASTNode) : PSPsiElement(node) {
    val expressionIdentifiers: List<PSExpressionIdentifier>
        get() = findChildrenByClass(PSExpressionIdentifier::class.java).toList()
}