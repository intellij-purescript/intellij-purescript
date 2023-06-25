package org.purescript.module.declaration.value.expression

import com.intellij.lang.ASTNode

import org.purescript.psi.PSPsiElement

class PSParens(node: ASTNode) : PSPsiElement(node), ExpressionAtom {
    val value get() = findChildByClass(Expression::class.java)
    override fun unify() {
        value?.inferType()?.let { unify(it) }
    }
}