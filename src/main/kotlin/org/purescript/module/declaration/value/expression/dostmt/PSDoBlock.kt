package org.purescript.module.declaration.value.expression.dostmt

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.module.declaration.value.expression.Expression
import org.purescript.psi.PSPsiElement

class PSDoBlock(node: ASTNode) : PSPsiElement(node), Expression {
    override val expressions: Sequence<Expression> get() = statements.expressions.asSequence()
    private val statements = childrenOfType<DoStatements>().single()
    override fun unify() = unify(statements.inferType())
}