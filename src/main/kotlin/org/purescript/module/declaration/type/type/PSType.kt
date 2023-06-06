package org.purescript.module.declaration.type.type

import com.intellij.psi.PsiElement
import org.purescript.typechecker.TypeCheckable

sealed interface PSType : TypeCheckable, PsiElement