package org.purescript.psi.expression

import com.intellij.psi.PsiElement

sealed interface ExpressionAtom: PsiElement, Expression {
    override fun getAtoms(): Sequence<ExpressionAtom> = sequenceOf(this)
}