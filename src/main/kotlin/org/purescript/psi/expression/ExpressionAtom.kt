package org.purescript.psi.expression

import com.intellij.psi.PsiReference
import org.purescript.psi.base.PSPsiElement

sealed interface ExpressionAtom {
    fun getReference(): PsiReference?
}