package org.purescript.module.declaration.value.expression.controll.caseof

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.psi.PSPsiElement
import org.purescript.module.declaration.value.binder.Binder

class PSCaseAlternative(node: ASTNode) : PSPsiElement(node) {
    val binders get() = childrenOfType<Binder>().flatMap { it.descendantBinders }
}