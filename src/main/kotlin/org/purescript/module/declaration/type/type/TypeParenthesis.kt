package org.purescript.module.declaration.type.type

import com.intellij.lang.ASTNode
import org.purescript.psi.PSPsiElement

class TypeParenthesis(node: ASTNode) : PSPsiElement(node), PSType {
    private val type get() = findChildByClass(PSType::class.java)
    override fun unify() = unify(type?.inferType() ?: error("could not parse content of parenthesis"))
}