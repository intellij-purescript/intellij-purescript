package org.purescript.module.declaration.type.type

import com.intellij.lang.ASTNode
import org.purescript.inference.inferType
import org.purescript.psi.PSPsiElement

class TypeApp(node: ASTNode) : PSPsiElement(node), PSType {
    private val types get() = findChildrenByClass(PSType::class.java)
    override fun unify() {
        val (kind, argument) = types
        unify(kind.inferType().app(argument.inferType()))
    }
}