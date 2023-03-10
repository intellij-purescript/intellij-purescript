package org.purescript.module.declaration.value.binder

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.util.childrenOfType
import org.purescript.psi.PSPsiElement

open class Binder(node: ASTNode) : PSPsiElement(node) {
    val descendantBinders get(): List<Binder> = 
        binderChildren.flatMap { it.descendantBinders } + listOf(this)
    val binderChildren get() = childrenOfType<Binder>()
    val namedDescendant get() = descendantBinders.filterIsInstance<PsiNamedElement>()
}