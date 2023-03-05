package org.purescript.psi.expression

import com.intellij.psi.PsiElement
import org.purescript.psi.declaration.classes.PSClassMemberList
import org.purescript.psi.module.Module

interface Expression: PsiElement {
    fun getAtoms(): Sequence<ExpressionAtom>
    val module: Module?
    fun getName(): String?
    val dependencies: Sequence<PSExpressionIdentifier>
        get() = getAtoms().filterIsInstance<PSExpressionIdentifier>().filter {
        val reference = it.reference.resolve() ?: return@filter true
        if (textRange.contains(reference.textRange)) false
        else when (reference.parent) {
            is Module , is PSClassMemberList -> false
            else -> true
        }
    }
}