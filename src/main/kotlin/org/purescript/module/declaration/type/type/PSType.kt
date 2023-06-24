package org.purescript.module.declaration.type.type

import com.intellij.psi.PsiElement
import org.purescript.inference.HasTypeId
import org.purescript.inference.Inferable
import org.purescript.inference.Unifiable

sealed interface PSType : HasTypeId, Unifiable, Inferable, PsiElement {
    override fun unify() {}
}