package org.purescript.psi.binder

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.util.childrenOfType
import org.purescript.psi.base.PSPsiElement

sealed class BinderAtom(node: ASTNode) : PSPsiElement(node) {
    val descendantBinders get(): List<BinderAtom> =
        childrenOfType<BinderAtom>().flatMap { it.descendantBinders } + listOf(this)
    val namedDescendant get() = descendantBinders.filterIsInstance<PsiNamedElement>()
}