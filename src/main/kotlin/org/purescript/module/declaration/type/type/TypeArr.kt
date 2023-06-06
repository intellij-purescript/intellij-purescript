package org.purescript.module.declaration.type.type

import com.intellij.lang.ASTNode
import org.purescript.psi.PSPsiElement
import org.purescript.typechecker.TypeCheckerType

class TypeArr(node: ASTNode) : PSPsiElement(node), PSType {

    private val types get() = findChildrenByClass(PSType::class.java)
    override fun checkType(): TypeCheckerType? {
        val types = types
        if (types.size != 2) return null
        val (first, second) = types
        val firstType = first.checkType() ?: return null
        val secondType = second.checkType() ?: return null
        val functionType = TypeCheckerType.TypeConstructor("Prim.Function")
        return TypeCheckerType.TypeApp(
            TypeCheckerType.TypeApp(functionType, firstType),
            secondType
        )
    }
}