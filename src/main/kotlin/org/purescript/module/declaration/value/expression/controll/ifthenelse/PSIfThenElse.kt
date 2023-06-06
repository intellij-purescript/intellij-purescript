package org.purescript.module.declaration.value.expression.controll.ifthenelse

import com.intellij.lang.ASTNode
import org.purescript.module.declaration.value.expression.Expression
import org.purescript.psi.PSPsiElement
import org.purescript.typechecker.TypeCheckerType

class PSIfThenElse(node: ASTNode) : PSPsiElement(node), Expression {
    override fun checkType(): TypeCheckerType? {
        return null
    }
}