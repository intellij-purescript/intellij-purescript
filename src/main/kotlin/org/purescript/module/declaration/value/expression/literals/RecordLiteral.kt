package org.purescript.module.declaration.value.expression.literals

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.inference.InferType

import org.purescript.module.declaration.value.expression.Expression
import org.purescript.module.declaration.value.expression.ExpressionAtom
import org.purescript.psi.PSPsiElement

class RecordLiteral(node: ASTNode) : PSPsiElement(node), ExpressionAtom {
    override val expressions: Sequence<Expression>
        get() = super.expressions + labels.asSequence().flatMap { it.expressions }
    val labels get() = childrenOfType<RecordLabel>()
    override fun unify() {
        unify(InferType.Record.app(InferType.RowList(labels.mapNotNull { 
            val name = it.name ?: return@mapNotNull null
            val expression = it.expression?.inferType() ?: return@mapNotNull null
            name to expression
        })))
    }
}
