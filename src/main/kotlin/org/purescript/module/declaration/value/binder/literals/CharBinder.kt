package org.purescript.module.declaration.value.binder.literals

import com.intellij.lang.ASTNode
import org.purescript.inference.InferType
import org.purescript.module.declaration.value.Similar
import org.purescript.module.declaration.value.binder.Binder

class CharBinder(node: ASTNode) : Binder(node) {
    override fun areSimilarTo(other: Similar): Boolean {
        return other is CharBinder && this.text == other.text
    }

    override val substitutedType: InferType get() = InferType.Char
}