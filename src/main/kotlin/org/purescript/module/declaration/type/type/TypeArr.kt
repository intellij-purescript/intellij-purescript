package org.purescript.module.declaration.type.type

import com.intellij.lang.ASTNode
import org.purescript.inference.Scope
import org.purescript.inference.Type
import org.purescript.psi.PSPsiElement
import org.purescript.typechecker.TypeCheckerType

class TypeArr(node: ASTNode) : PSPsiElement(node), PSType {

    private val types get() = findChildrenByClass(PSType::class.java)
    override fun checkType(): TypeCheckerType? {
        val types = types
        if (types.size != 2) return null
        val (first, second) = types
        return TypeCheckerType.function(
            first.checkType() ?: return null,
            second.checkType() ?: return null
        )
    }

    override fun infer(scope: Scope): Type {
        val (first, second) = types
        return Type.function(first.infer(scope), second.infer(scope))
    }
}