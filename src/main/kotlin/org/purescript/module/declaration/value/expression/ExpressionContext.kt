package org.purescript.module.declaration.value.expression

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement

interface ExpressionContext: PsiElement {
    val expressionDeclarations: Sequence<PsiNamedElement>
}