package org.purescript.psi.expression

import com.intellij.lang.ASTNode
import org.purescript.psi.base.PSPsiElement

class PSValue(node: ASTNode) : PSPsiElement(node), Expression {
    val expressionAtoms
        get() = sequence {
            var steps = children.asList()
            while (steps.isNotEmpty()) {
                this.yieldAll(steps)
                steps = steps.flatMap { it.children.asList() }
            }
        }.filterIsInstance<ExpressionAtom>().toList()

    override fun getAtoms(): Sequence<ExpressionAtom> = expressionAtoms.asSequence()
}