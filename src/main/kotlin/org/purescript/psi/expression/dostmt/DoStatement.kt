package org.purescript.psi.expression.dostmt

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import org.purescript.psi.binder.BinderAtom

interface DoStatement: PsiElement {
    val binders: List<BinderAtom>
    val namedElements: List<PsiNamedElement> get() = 
        binders.filterIsInstance<PsiNamedElement>()
}