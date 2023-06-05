package org.purescript.module.declaration.value.expression

import com.intellij.lang.ASTNode
import org.purescript.psi.PSPsiElement
import org.purescript.typechecker.TypeCheckerType

class PSTypeHole(node: ASTNode) : PSPsiElement(node), ExpressionAtom {
    override fun checkType(): TypeCheckerType? = null
}