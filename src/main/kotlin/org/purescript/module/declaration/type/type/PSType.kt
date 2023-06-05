package org.purescript.module.declaration.type.type

import com.intellij.psi.PsiElement
import org.purescript.typechecker.TypeCheckable
import org.purescript.typechecker.TypeCheckerType

sealed interface PSType : TypeCheckable, PsiElement {
    override fun checkType(): TypeCheckerType? = null
}