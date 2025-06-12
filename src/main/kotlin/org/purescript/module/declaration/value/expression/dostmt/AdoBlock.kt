package org.purescript.module.declaration.value.expression.dostmt

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.module.declaration.value.ValueNamespace
import org.purescript.module.declaration.value.expression.Expression
import org.purescript.psi.PSPsiElement

class AdoBlock(node: ASTNode) : PSPsiElement(node), ValueNamespace, Expression {
    private val statements get() = childrenOfType<DoStatements>().single()
    override fun unify() {
        statements.unify()
        unify(childrenOfType<Expression>().single().inferType())
    }
    override val valueNames get() = statements.statements.lastOrNull()?.valueNamesAhead ?: emptySequence()
}