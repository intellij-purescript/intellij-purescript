package org.purescript.psi.expression.dostmt

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import org.purescript.psi.binder.Binder

interface DoStatement: PsiElement {
    val binders: List<Binder>
    val namedElements: List<PsiNamedElement> get() = 
        binders.filterIsInstance<PsiNamedElement>()
}