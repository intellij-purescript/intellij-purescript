package org.purescript.module.declaration.value.expression.dostmt

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.util.childrenOfType
import org.purescript.inference.Inferable
import org.purescript.module.declaration.value.ValueNamespace
import org.purescript.module.declaration.value.expression.Expression
import org.purescript.psi.PSPsiElement

/**
 * Represents one or many do statements
 *
 * They are grouped like so (DoStatements, DoStatement) or just DoStatement
 */
class DoStatements(node: ASTNode) : PSPsiElement(node), Inferable {
    val statements get() = childrenOfType<DoStatement>()
    val expressions: List<Expression> get() = statements.flatMap { it.expressions }
    val binders get() = statements.flatMap { it.binders }
    override fun unify() {
        val other = statements.map { it.inferType() }.lastOrNull()
        other?.let { unify(it) }
    }
}