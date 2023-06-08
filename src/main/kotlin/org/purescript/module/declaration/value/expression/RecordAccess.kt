package org.purescript.module.declaration.value.expression

import com.intellij.lang.ASTNode
import org.purescript.psi.PSPsiElement
import org.purescript.typechecker.TypeCheckerType

class RecordAccess(node: ASTNode) : PSPsiElement(node), Expression {
    override fun checkType(): TypeCheckerType? {
        return null
    }
}