package org.purescript.psi.parameters

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.psi.base.PSPsiElement
import org.purescript.psi.binder.Binder

class Parameter(node: ASTNode) : PSPsiElement(node) {
    val parameterBinders = childrenOfType<Binder>()
}