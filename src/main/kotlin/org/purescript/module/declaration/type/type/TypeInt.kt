package org.purescript.module.declaration.type.type

import com.intellij.lang.ASTNode
import org.purescript.inference.InferType
import org.purescript.psi.PSPsiElement

class TypeInt(node: ASTNode) : PSPsiElement(node), PSType {
    private val value get() = text.toInt()
    override val substitutedType: InferType get() = InferType.Int
}