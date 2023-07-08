package org.purescript.module.declaration.value.expression.dostmt

import com.intellij.psi.PsiElement
import com.intellij.psi.util.childrenOfType
import org.purescript.module.declaration.value.Similar
import org.purescript.module.declaration.value.binder.Binder
import org.purescript.module.declaration.value.expression.Expression

interface DoStatement : PsiElement, Similar {
    val expressions get() = childrenOfType<Expression>().flatMap { it.expressions }
    val doStatementsParent get() = parent as? DoStatements
    val statements: List<DoStatement> get() = emptyList()
    val binders: List<Binder>
}