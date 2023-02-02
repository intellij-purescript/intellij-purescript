package org.purescript.psi.expression

import com.intellij.psi.PsiReference

sealed interface ExpressionAtom {
    fun getReference(): PsiReference?
}