package org.purescript.module.declaration.type

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement

interface TypeNamespace: PsiElement {
    val typeNames: Sequence<PsiNamedElement>
}