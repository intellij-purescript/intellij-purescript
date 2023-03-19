package org.purescript.module.declaration.value.expression.controll.caseof

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.module.declaration.value.Similar
import org.purescript.module.declaration.value.ValueNamespace
import org.purescript.module.declaration.value.binder.Binder
import org.purescript.module.declaration.value.expression.Expression
import org.purescript.module.declaration.value.expression.namespace.PSExpressionWhere
import org.purescript.psi.PSPsiElement

class CaseAlternative(node: ASTNode) : PSPsiElement(node), ValueNamespace, Similar {
    private val where get() = childrenOfType<PSExpressionWhere>().asSequence()
    private val binders get() = childrenOfType<Binder>().asSequence()
    override val valueNames
        get() = binders.flatMap { it.namedDescendant } +
                where.flatMap { it.valueNames }

    val expressions: Sequence<Expression>
        get() = childrenOfType<Expression>().asSequence().flatMap { it.expressions }
}