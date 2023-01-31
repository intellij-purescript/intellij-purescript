package org.purescript.psi.expression

import com.intellij.lang.ASTNode
import org.purescript.psi.base.PSPsiElement

class PSValue(node: ASTNode) : PSPsiElement(node) {
    val expressionAtoms
        get() = findChildrenByClass(ExpressionAtom::class.java).toList()
    
}