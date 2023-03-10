package org.purescript.module.declaration.value.expression.dostmt

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import org.purescript.module.declaration.value.binder.Binder

interface DoStatement: PsiElement {
    val binders: List<Binder>
    val namedElements: List<PsiNamedElement> get() = 
        binders.filterIsInstance<PsiNamedElement>()
}