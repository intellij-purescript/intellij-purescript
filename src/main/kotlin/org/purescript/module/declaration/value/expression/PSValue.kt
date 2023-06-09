package org.purescript.module.declaration.value.expression

import com.intellij.lang.ASTNode
import org.purescript.psi.PSPsiElement
import org.purescript.typechecker.TypeCheckable
import org.purescript.typechecker.TypeCheckerType

class PSValue(node: ASTNode) : PSPsiElement(node), Expression, TypeCheckable {

    override fun checkType(): TypeCheckerType? =
        findChildrenByClass(Expression::class.java)
            .singleOrNull()
            ?.checkType()
}