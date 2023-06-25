package org.purescript.module.declaration.value.parameters

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.inference.Inferable
import org.purescript.inference.Unifiable
import org.purescript.module.declaration.value.binder.Binder
import org.purescript.psi.PSPsiElement

class Parameter(node: ASTNode) : PSPsiElement(node), Inferable, Unifiable {
    val parameterBinders get() = childrenOfType<Binder>()
    val binder get() = parameterBinders.first()
    override fun unify() = unify(binder.substitutedType)
}