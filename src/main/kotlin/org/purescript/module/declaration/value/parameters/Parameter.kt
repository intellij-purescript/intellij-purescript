package org.purescript.module.declaration.value.parameters

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.psi.PSPsiElement
import org.purescript.module.declaration.value.binder.Binder

class Parameter(node: ASTNode) : PSPsiElement(node) {
    val parameterBinders = childrenOfType<Binder>()
}