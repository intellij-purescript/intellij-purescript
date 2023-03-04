package org.purescript.psi.expression

import com.intellij.psi.PsiElement
import org.purescript.psi.module.Module

interface Expression: PsiElement {
    fun getAtoms(): Sequence<ExpressionAtom>
    val module: Module?
    fun getName(): String?
}