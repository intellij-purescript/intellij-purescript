package org.purescript.module.declaration.type.type

import com.intellij.lang.ASTNode
import org.purescript.inference.InferType
import org.purescript.inference.inferType
import org.purescript.psi.PSPsiElement

class TypeArr(node: ASTNode) : PSPsiElement(node), PSType {
    private val types get() = findChildrenByClass(PSType::class.java)
    override fun unify() {
        val (first, second) = types
        val parameter = first.inferType()
        val ret = second.inferType()
        unify(InferType.function(parameter, ret))
    }
}