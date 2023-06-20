package org.purescript.module.declaration.type.type

import com.intellij.lang.ASTNode
import org.purescript.inference.Scope
import org.purescript.inference.Type
import org.purescript.psi.PSPsiElement
import org.purescript.typechecker.TypeCheckerType

class PSConstrainedType(node: ASTNode) : PSPsiElement(node), PSType {
    override fun checkType(): TypeCheckerType? = null
    override fun infer(scope: Scope): Type = TODO("Not yet implemented")
}