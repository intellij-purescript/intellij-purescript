package org.purescript.module.declaration.value.parameters

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.inference.Inferable
import org.purescript.inference.Scope
import org.purescript.inference.Type
import org.purescript.module.declaration.value.binder.Binder
import org.purescript.psi.PSPsiElement

class Parameter(node: ASTNode) : PSPsiElement(node), Inferable {
    val parameterBinders get() = childrenOfType<Binder>()
    val binder get() = parameterBinders.first()
    override fun infer(scope: Scope): Type = binder.infer(scope)
}