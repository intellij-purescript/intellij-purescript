package org.purescript.module.declaration.type.type

import com.intellij.lang.ASTNode
import org.purescript.psi.PSPsiElement
import org.purescript.typechecker.TypeCheckerType

class TypeInt(node: ASTNode) : PSPsiElement(node), PSType {
    private val value get() = text.toInt()
    override fun checkType(): TypeCheckerType = TypeCheckerType.TypeLevelInt(value)
}