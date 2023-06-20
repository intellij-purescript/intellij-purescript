package org.purescript.module.declaration.type.type

import com.intellij.lang.ASTNode
import org.purescript.inference.Scope
import org.purescript.inference.Type
import org.purescript.psi.PSPsiElement
import org.purescript.typechecker.TypeCheckerType

class TypeString(node: ASTNode) : PSPsiElement(node), PSType {
    override fun checkType(): TypeCheckerType = TypeCheckerType.TypeLevelString(text)
    override fun infer(scope: Scope): Type = Type.String
}