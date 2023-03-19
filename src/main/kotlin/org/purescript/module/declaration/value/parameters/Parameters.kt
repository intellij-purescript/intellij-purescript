package org.purescript.module.declaration.value.parameters

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.module.declaration.value.binder.VarBinder
import org.purescript.psi.PSPsiElement

class Parameters(node: ASTNode) : PSPsiElement(node) {
    val valueNames get() = parameterBinders.asSequence().flatMap { it.namedDescendant }
    val parameterBinders get() = parameters.flatMap { it.parameterBinders }
    val varBinderParameters get() = parameterBinders.filterIsInstance<VarBinder>()
    val parameters get() = childrenOfType<Parameter>()
}