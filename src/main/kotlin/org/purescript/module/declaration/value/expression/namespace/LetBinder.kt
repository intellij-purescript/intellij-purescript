package org.purescript.module.declaration.value.expression.namespace

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.module.declaration.value.ValueNamespace
import org.purescript.module.declaration.value.binder.Binder
import org.purescript.module.declaration.value.expression.Expression
import org.purescript.psi.PSPsiElement

class LetBinder(node: ASTNode) : PSPsiElement(node), Expression, ValueNamespace {
    private val binders = childrenOfType<Binder>().asSequence()
    private val where = childrenOfType<PSExpressionWhere>().asSequence()
    val namedBinders = binders.flatMap { it.namedDescendant }
    override val valueNames get() = where.flatMap { it.valueNames } + namedBinders
}