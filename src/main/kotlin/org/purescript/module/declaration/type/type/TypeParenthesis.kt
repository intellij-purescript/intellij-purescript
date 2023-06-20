package org.purescript.module.declaration.type.type

import com.intellij.lang.ASTNode
import org.purescript.inference.Scope
import org.purescript.inference.Type
import org.purescript.psi.PSPsiElement
import org.purescript.typechecker.TypeCheckerType

class TypeParenthesis(node: ASTNode) : PSPsiElement(node), PSType {
    private val type get() = findChildByClass(PSType::class.java)
    override fun checkType(): TypeCheckerType? {
        return type?.checkType()
    }

    override fun infer(scope: Scope): Type = type!!.infer(scope)
}