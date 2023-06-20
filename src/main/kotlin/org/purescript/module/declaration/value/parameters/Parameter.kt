package org.purescript.module.declaration.value.parameters

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.inference.Inferable
import org.purescript.inference.Scope
import org.purescript.inference.Type
import org.purescript.module.declaration.value.binder.Binder
import org.purescript.psi.PSPsiElement
import org.purescript.typechecker.TypeCheckable

class Parameter(node: ASTNode) : PSPsiElement(node),
    TypeCheckable,
    Inferable {
    val parameterBinders get() = childrenOfType<Binder>()
    val binder get() = parameterBinders.first()
    override fun checkUsageType() = binder.checkType()
    override fun checkReferenceType() = when (val parameters = parent) {
        is Parameters -> parameters.checkReferenceType()?.let {
            val index = parameters.childrenOfType<Parameter>().indexOf(this)
            var type = it
            for (i in 1..index) {
                type = type.ret ?: return@let null
            }
            type.parameter
        }

        else -> null
    }

    override fun infer(scope: Scope): Type {
        return binder.infer(scope)
    }
}