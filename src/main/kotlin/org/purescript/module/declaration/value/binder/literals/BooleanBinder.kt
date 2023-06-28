package org.purescript.module.declaration.value.binder.literals

import com.intellij.lang.ASTNode
import org.purescript.inference.InferType
import org.purescript.module.declaration.value.Similar
import org.purescript.module.declaration.value.binder.Binder

class BooleanBinder(node: ASTNode) : Binder(node) {
    override fun areSimilarTo(other: Similar): Boolean {
        return other is BooleanBinder && this.text == other.text
    }
    override val substitutedType: InferType get() = InferType.Boolean
}