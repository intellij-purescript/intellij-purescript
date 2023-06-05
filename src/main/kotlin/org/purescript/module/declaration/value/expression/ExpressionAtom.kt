package org.purescript.module.declaration.value.expression

import com.intellij.psi.PsiElement
import org.purescript.typechecker.TypeCheckable

interface ExpressionAtom: PsiElement, Expression, TypeCheckable