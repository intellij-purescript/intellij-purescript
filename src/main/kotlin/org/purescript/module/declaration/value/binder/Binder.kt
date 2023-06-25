package org.purescript.module.declaration.value.binder

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.util.parentOfType
import org.purescript.inference.Inferable
import org.purescript.inference.Unifiable
import org.purescript.module.declaration.value.Similar
import org.purescript.module.declaration.value.ValueNamespace
import org.purescript.psi.PSPsiElement

open class Binder(node: ASTNode) : PSPsiElement(node),
    Similar,
    Inferable,
    Unifiable {
    val descendantBinders
        get(): List<Binder> =
            binderChildren.flatMap { it.descendantBinders } + listOf(this)
    val binderChildren get() = childrenOfType<Binder>()
    val namedDescendant get() = descendantBinders.filterIsInstance<PsiNamedElement>()
    override fun getUseScope(): SearchScope =
        LocalSearchScope(parentOfType<ValueNamespace>()?.scopes ?: arrayOf(containingFile))

    override fun unify() {}
}