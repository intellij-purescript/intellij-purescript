package org.purescript.module.declaration.type

import com.intellij.lang.ASTNode
import org.purescript.psi.PSPsiElement
import org.purescript.typechecker.TypeCheckable
import org.purescript.typechecker.TypeCheckerType

class TypeWildcard(node: ASTNode) : PSPsiElement(node), TypeCheckable {
    override fun checkType(): TypeCheckerType? = null
}