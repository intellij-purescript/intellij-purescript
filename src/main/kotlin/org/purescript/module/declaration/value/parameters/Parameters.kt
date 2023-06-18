package org.purescript.module.declaration.value.parameters

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.module.declaration.value.ValueDecl
import org.purescript.module.declaration.value.binder.VarBinder
import org.purescript.psi.PSPsiElement
import org.purescript.typechecker.TypeCheckable
import org.purescript.typechecker.TypeCheckerType

class Parameters(node: ASTNode) : PSPsiElement(node), TypeCheckable {
    val valueNames get() = parameterBinders.asSequence().flatMap { it.namedDescendant }
    val parameterBinders get() = parameters.flatMap { it.parameterBinders }
    val varBinderParameters get() = parameterBinders.filterIsInstance<VarBinder>()
    val parameters get() = childrenOfType<Parameter>()
    override fun checkUsageType() = parameters
        .map { it.checkUsageType() }
        .reduceOrNull { a, b -> a?.arrow(b ?: return@reduceOrNull null) }

    override fun checkReferenceType() = 
        (parent as? ValueDecl)
        ?.checkReferenceType()
            ?.let { 
                var f = it
                val types = mutableListOf<TypeCheckerType>()
                for (parameter in parameters) {
                    types.add(f.parameter ?: return@let null)
                    f = f.ret ?: return@let null
                }
                types.reduce {acc, t -> acc.arrow(t)}
            }
}