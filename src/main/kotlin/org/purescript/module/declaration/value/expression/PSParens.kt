package org.purescript.module.declaration.value.expression

import com.intellij.lang.ASTNode
import org.purescript.inference.Scope
import org.purescript.inference.InferType
import org.purescript.psi.PSPsiElement

class PSParens(node: ASTNode) : PSPsiElement(node), ExpressionAtom {
    val value get() = findChildByClass(Expression::class.java)
    override fun infer(scope: Scope): InferType = value?.infer(scope) ?: scope.newUnknown()
}