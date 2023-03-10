package org.purescript.psi.expression.controll.caseof

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.psi.base.PSPsiElement
import org.purescript.psi.binder.Binder

class PSCaseAlternative(node: ASTNode) : PSPsiElement(node) {
    val binders get() = childrenOfType<Binder>().flatMap { it.descendantBinders }
}