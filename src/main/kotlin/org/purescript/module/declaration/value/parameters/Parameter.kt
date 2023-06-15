package org.purescript.module.declaration.value.parameters

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.module.declaration.value.binder.Binder
import org.purescript.psi.PSPsiElement
import org.purescript.typechecker.TypeCheckable

class Parameter(node: ASTNode) : PSPsiElement(node), TypeCheckable {
    val parameterBinders get() = childrenOfType<Binder>()
    val binder get() = parameterBinders.first()
    override fun checkType() = binder.checkType()
}