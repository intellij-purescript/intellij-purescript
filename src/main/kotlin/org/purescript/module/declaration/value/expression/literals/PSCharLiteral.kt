package org.purescript.module.declaration.value.expression.literals

import com.intellij.lang.ASTNode
import org.purescript.inference.Scope
import org.purescript.inference.InferType
import org.purescript.module.declaration.value.expression.ExpressionAtom
import org.purescript.psi.PSPsiElement

class PSCharLiteral(node: ASTNode) : PSPsiElement(node), ExpressionAtom {
    override fun infer(scope: Scope): InferType = InferType.Char
}