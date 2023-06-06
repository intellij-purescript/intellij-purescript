package org.purescript.module.declaration.type.type

import com.intellij.lang.ASTNode
import org.purescript.module.declaration.type.PSTypeVarName
import org.purescript.psi.PSPsiElement
import org.purescript.typechecker.TypeCheckerType

class ForAll(node: ASTNode) : PSPsiElement(node), PSType {
    private val type get() = findChildByClass(PSType::class.java)
    private val typeVars get() = findChildrenByClass(PSTypeVarName::class.java)
    override fun checkType(): TypeCheckerType? {
        return typeVars
            .mapNotNull { it.name }
            .fold(type?.checkType() ?: return null) { acc: TypeCheckerType, name: String ->
                TypeCheckerType.ForAll(name, acc)
            }
    }
}