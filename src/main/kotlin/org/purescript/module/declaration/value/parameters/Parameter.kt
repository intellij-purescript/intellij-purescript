package org.purescript.module.declaration.value.parameters

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.module.declaration.value.binder.Binder
import org.purescript.psi.PSPsiElement

class Parameter(node: ASTNode) : PSPsiElement(node) {
    val parameterBinders get() = childrenOfType<Binder>()
}