package org.purescript.module.declaration.value

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement

interface ValueNamespace: PsiElement {
    val valueNames: Sequence<PsiNamedElement>
}