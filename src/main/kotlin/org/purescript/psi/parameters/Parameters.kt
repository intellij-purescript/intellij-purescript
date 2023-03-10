package org.purescript.psi.parameters

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.psi.base.PSPsiElement
import org.purescript.psi.binder.leaf.VarBinder

class Parameters(node: ASTNode) : PSPsiElement(node) {
    val namedDescendant = parameterBinders.flatMap { it.namedDescendant }
    val parameterBinders get() = parameters.flatMap { it.parameterBinders }
    val varBinderParameters get() = parameterBinders.filterIsInstance<VarBinder>()
    val parameters get() = childrenOfType<Parameter>()
}