package org.purescript.module.declaration.value.expression.namespace

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.inference.Scope
import org.purescript.inference.InferType
import org.purescript.module.declaration.value.ValueNamespace
import org.purescript.module.declaration.value.binder.Binder
import org.purescript.module.declaration.value.expression.Expression
import org.purescript.psi.PSPsiElement

class LetBinder(node: ASTNode) : PSPsiElement(node), Expression, ValueNamespace {
    private val binders get()  = childrenOfType<Binder>().asSequence()
    private val where get()  = childrenOfType<PSExpressionWhere>().asSequence()
    val namedBinders get() = binders.flatMap { it.namedDescendant }
    override val valueNames get() = where.flatMap { it.valueNames } + namedBinders
    override fun infer(scope: Scope): InferType {
        TODO("Implement infer for ${this.node.elementType.debugName}")
    }
}