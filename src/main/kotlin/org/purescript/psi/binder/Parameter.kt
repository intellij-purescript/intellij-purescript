package org.purescript.psi.binder

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.psi.base.PSPsiElement

class Parameter(node: ASTNode) : PSPsiElement(node) {
    val descendantBinders get() = childrenBinders.flatMap { it.descendantBinders }
    val childrenBinders = childrenOfType<Binder>()
}