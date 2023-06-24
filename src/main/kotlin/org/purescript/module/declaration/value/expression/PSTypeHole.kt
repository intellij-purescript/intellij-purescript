package org.purescript.module.declaration.value.expression

import com.intellij.lang.ASTNode
import org.purescript.inference.Scope
import org.purescript.inference.InferType
import org.purescript.psi.PSPsiElement

class PSTypeHole(node: ASTNode) : PSPsiElement(node), ExpressionAtom {
    override fun infer(scope: Scope): InferType = scope.newUnknown()
}