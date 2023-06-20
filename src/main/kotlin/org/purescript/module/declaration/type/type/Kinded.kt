package org.purescript.module.declaration.type.type

import com.intellij.lang.ASTNode
import org.purescript.inference.Scope
import org.purescript.inference.Type
import org.purescript.psi.PSPsiElement
import org.purescript.typechecker.TypeCheckable
import org.purescript.typechecker.TypeCheckerType

class Kinded(node: ASTNode) : PSPsiElement(node), TypeCheckable, PSType {
    override fun checkType(): TypeCheckerType? = null
    override fun infer(scope: Scope): Type {
        TODO("Not yet implemented")
    }
}