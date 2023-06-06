package org.purescript.module.declaration.value.expression

import com.intellij.lang.ASTNode
import org.purescript.psi.PSPsiElement
import org.purescript.typechecker.TypeCheckable
import org.purescript.typechecker.TypeCheckerType

class PSValue(node: ASTNode) : PSPsiElement(node), Expression, TypeCheckable {
    val expressionAtoms
        get() = sequence {
            var steps = children.asList()
            while (steps.isNotEmpty()) {
                this.yieldAll(steps)
                steps = steps.flatMap { it.children.asList() }
            }
        }.filterIsInstance<ExpressionAtom>().toList()

    override fun checkType(): TypeCheckerType? =
        findChildrenByClass(Expression::class.java)
            .singleOrNull()
            ?.checkType()
}