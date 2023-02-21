package org.purescript.psi.expression

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import org.purescript.psi.base.PSPsiElement

class PSValue(node: ASTNode) : PSPsiElement(node) {
    val expressionAtoms
        get() = sequence {
            var steps = children.asList()
            while (steps.isNotEmpty()) {
                this.yieldAll(steps)
                steps = steps.flatMap { it.children.asList() }
            }
        }.filterIsInstance<ExpressionAtom>().toList()
}