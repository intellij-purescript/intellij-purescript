package org.purescript.module.declaration.value.expression.dostmt

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.inference.Inferable
import org.purescript.module.declaration.value.binder.Binder
import org.purescript.module.declaration.value.expression.Expression
import org.purescript.psi.PSPsiElement

class PSDoNotationValue(node: ASTNode) : PSPsiElement(node), DoStatement, Inferable {
    override val binders get() = emptyList<Binder>()
    private val expr get() = childrenOfType<Expression>().single()
    override fun unify() = unify(expr.inferType())
}