package org.purescript.module.declaration.type.type

import com.intellij.lang.ASTNode
import org.purescript.psi.PSPsiElement
import org.purescript.typechecker.TypeCheckerType

class ForAll(node: ASTNode) : PSPsiElement(node), PSType {
    override fun checkType(): TypeCheckerType? {
        return null
    }
}