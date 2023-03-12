package org.purescript.module.declaration.value.expression.dostmt

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.util.siblings
import org.purescript.module.declaration.value.ValueNamespace
import org.purescript.module.declaration.value.binder.Binder

interface DoStatement : PsiElement, ValueNamespace {
    val binders: Sequence<Binder>
    val previousDoStatement: DoStatement?
        get() = siblings(forward = false, withSelf = false)
            .filterIsInstance<DoStatement>().firstOrNull()
    val nextDoStatement: DoStatement?
        get() = siblings(forward = true, withSelf = false)
            .filterIsInstance<DoStatement>().firstOrNull()
    override val valueNames: Sequence<PsiNamedElement>
        get() = binders.filterIsInstance<PsiNamedElement>() +
                (previousDoStatement?.valueNames ?: emptySequence())
    override val scopes: Array<PsiElement>
        get() = (sequenceOf(this) + (nextDoStatement?.scopes?.asSequence() ?: emptySequence<PsiElement>()))
            .toList()
            .toTypedArray()
}