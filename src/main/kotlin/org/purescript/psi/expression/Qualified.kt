package org.purescript.psi.expression

import com.intellij.psi.PsiElement

interface Qualified: PsiElement {
    val qualifierName: String?
}