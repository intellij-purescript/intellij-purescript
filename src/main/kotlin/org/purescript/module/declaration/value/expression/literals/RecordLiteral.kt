package org.purescript.module.declaration.value.expression.literals

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.inference.Scope
import org.purescript.inference.Type
import org.purescript.module.declaration.value.expression.Expression
import org.purescript.module.declaration.value.expression.ExpressionAtom
import org.purescript.psi.PSPsiElement

class RecordLiteral(node: ASTNode) : PSPsiElement(node), ExpressionAtom {
    override val expressions: Sequence<Expression>
        get() = super.expressions + labels.asSequence().flatMap { it.expressions }
    val labels get() = childrenOfType<RecordLabel>()
    override fun infer(scope: Scope): Type {
        return Type.Record.app(Type.Row(labels.mapNotNull { 
            (it.name ?: return@mapNotNull null) to (it.expression?.infer(scope) ?: scope.newUnknown())
        }))
    }
}
