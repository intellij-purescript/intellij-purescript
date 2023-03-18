package org.purescript.psi

import com.intellij.psi.PsiElement
import org.purescript.module.declaration.value.expression.Expression
import org.purescript.module.declaration.value.expression.identifier.Argument

interface InlinableElement: PsiElement {
    fun inline(arguments: List<Argument>): Expression
}