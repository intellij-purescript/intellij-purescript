package org.purescript.psi.binder

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.util.childrenOfType
import org.purescript.psi.base.PSPsiElement

open class Binder(node: ASTNode) : PSPsiElement(node) {
    val descendantBinders get(): List<Binder> = 
        binderChildren.flatMap { it.descendantBinders } + listOf(this)
    val binderChildren get() = childrenOfType<Binder>()
    val namedDescendant get() = descendantBinders.filterIsInstance<PsiNamedElement>()
}