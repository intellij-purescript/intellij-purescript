package org.purescript.module.declaration.value.expression.controll

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.module.declaration.value.Similar
import org.purescript.module.declaration.value.ValueNamespace
import org.purescript.module.declaration.value.binder.Binder
import org.purescript.psi.PSPsiElement

class Guard(node: ASTNode) : PSPsiElement(node), ValueNamespace, Similar {
    override val valueNames get() = binders.flatMap { it.namedDescendant }
    val binders get() = childrenOfType<Binder>().asSequence()
}