package org.purescript.module.declaration.value.expression.literals

import com.intellij.lang.ASTNode
import org.purescript.module.declaration.value.expression.ExpressionAtom
import org.purescript.psi.PSPsiElement
import org.purescript.typechecker.TypeCheckerType
import org.purescript.typechecker.TypeConstructor

class PSCharLiteral(node: ASTNode) : PSPsiElement(node), ExpressionAtom {
    override fun checkType(): TypeCheckerType? {
        return TypeConstructor("Prim.Char")
    }
}