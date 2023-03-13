package org.purescript.module.declaration.value

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement

interface ValueNamespace: PsiElement {
    val scopes: Array<PsiElement> get() = arrayOf(this)
    val valueNames: Sequence<PsiNamedElement>
    val constructors: Sequence<PsiNamedElement> get() = emptySequence()
}