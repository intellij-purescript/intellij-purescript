package org.purescript.module.declaration.value.expression.dostmt

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.util.siblings
import org.purescript.inference.Inferable
import org.purescript.module.declaration.value.Similar
import org.purescript.module.declaration.value.ValueNamespace
import org.purescript.module.declaration.value.binder.Binder
import org.purescript.module.declaration.value.expression.Expression

interface DoStatement : PsiElement, Similar, ValueNamespace, Inferable {
    val expressions get() = childrenOfType<Expression>().flatMap { it.expressions }
    val binders: List<Binder>
    val previous get(): DoStatement? {
        val statements = siblings(forward = false, withSelf = false).filterIsInstance<DoStatement>()
        return statements.firstOrNull()
    }
    val valueNamesAhead: Sequence<PsiNamedElement>
    override val valueNames: Sequence<PsiNamedElement> get() =
        previous?.valueNamesAhead ?: emptySequence()
}