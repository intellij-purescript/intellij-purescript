package org.purescript.module.declaration.value.expression.namespace

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.module.declaration.value.ValueNamespace
import org.purescript.module.declaration.value.binder.Binder
import org.purescript.module.declaration.value.expression.Expression
import org.purescript.psi.PSPsiElement

class LetBinder(node: ASTNode) : PSPsiElement(node), Expression, ValueNamespace {
    private val binder get()  = childrenOfType<Binder>().single()
    private val expr get()  = childrenOfType<Expression>().single()
    private val where get()  = childrenOfType<PSExpressionWhere>().singleOrNull()
    val namedBinders get() = binder.namedDescendant 
    override val valueNames get() = (where?.valueNames ?: emptySequence()) + namedBinders
    override fun unify() = unify(binder.inferType(), expr.inferType())
}