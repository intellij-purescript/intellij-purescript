package org.purescript.module.declaration.value.expression

import com.intellij.psi.PsiElement

interface Qualified: PsiElement {
    fun getName(): String
    val qualifierName: String?
}