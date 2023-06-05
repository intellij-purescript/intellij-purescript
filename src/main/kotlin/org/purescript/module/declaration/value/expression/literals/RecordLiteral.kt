package org.purescript.module.declaration.value.expression.literals

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.module.declaration.value.expression.Expression
import org.purescript.module.declaration.value.expression.ExpressionAtom
import org.purescript.psi.PSPsiElement
import org.purescript.typechecker.TypeCheckerType

class RecordLiteral(node: ASTNode) : PSPsiElement(node), ExpressionAtom {
    override val expressions: Sequence<Expression>
        get() = super.expressions +
                childrenOfType<RecordLabel>().asSequence().flatMap { it.expressions }

    override fun checkType(): TypeCheckerType? = null
}
