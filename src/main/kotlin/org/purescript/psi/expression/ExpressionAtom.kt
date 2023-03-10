package org.purescript.psi.expression

import com.intellij.psi.PsiElement

interface ExpressionAtom: PsiElement, Expression {
    override fun getAtoms(): Sequence<ExpressionAtom> = sequenceOf(this)
}