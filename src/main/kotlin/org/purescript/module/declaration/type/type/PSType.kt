package org.purescript.module.declaration.type.type

import com.intellij.psi.PsiElement
import org.purescript.inference.Inferable
import org.purescript.typechecker.TypeCheckable

sealed interface PSType : TypeCheckable, Inferable, PsiElement