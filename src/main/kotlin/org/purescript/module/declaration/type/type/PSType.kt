package org.purescript.module.declaration.type.type

import com.intellij.psi.PsiElement
import org.purescript.inference.HasTypeId
import org.purescript.inference.Inferable

sealed interface PSType : HasTypeId, Inferable, PsiElement {
    override fun unify() {}
}