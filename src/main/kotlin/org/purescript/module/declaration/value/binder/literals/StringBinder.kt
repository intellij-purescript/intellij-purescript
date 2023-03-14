package org.purescript.module.declaration.value.binder.literals

import com.intellij.lang.ASTNode
import org.purescript.module.declaration.value.Similar
import org.purescript.module.declaration.value.binder.Binder

class StringBinder(node: ASTNode) : Binder(node) {
    override fun areSimilarTo(other: Similar): Boolean {
        return other is StringBinder && this.text == other.text
    }
}