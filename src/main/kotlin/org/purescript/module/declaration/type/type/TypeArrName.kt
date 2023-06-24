package org.purescript.module.declaration.type.type

import com.intellij.lang.ASTNode
import org.purescript.inference.Scope
import org.purescript.inference.InferType
import org.purescript.psi.PSPsiElement

class TypeArrName(node: ASTNode) : PSPsiElement(node), PSType {
    private val types get() = findChildrenByClass(PSType::class.java)
    override fun infer(scope: Scope): InferType {
        TODO("Not yet implemented")
    }
}
