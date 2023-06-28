package org.purescript.module.declaration.value.binder

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.inference.InferType

/**
 * The node `Box a` in the code
 * 
 * ```purescript
 * f (Box a) = a
 * ```
 */
class AppBinder(node: ASTNode) : Binder(node) {
    val binders = childrenOfType<Binder>()
    override fun unify() {
        val (kind, argument) = binders.map { it.inferType() }
        unify(kind, InferType.function(argument, substitutedType))
    }
}