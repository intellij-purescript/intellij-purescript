package org.purescript.module.declaration.value.binder.literals

import com.intellij.lang.ASTNode
import org.purescript.module.declaration.value.Similar
import org.purescript.module.declaration.value.binder.Binder

class NumberBinder(node: ASTNode) : Binder(node) {
    override fun areSimilarTo(other: Similar): Boolean {
        return other is NumberBinder && this.text == other.text
    }
}
