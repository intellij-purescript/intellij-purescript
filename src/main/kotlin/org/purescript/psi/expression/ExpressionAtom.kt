package org.purescript.psi.expression

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference

sealed interface ExpressionAtom: PsiElement {
}