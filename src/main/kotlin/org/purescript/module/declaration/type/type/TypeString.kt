package org.purescript.module.declaration.type.type

import com.intellij.lang.ASTNode
import org.purescript.inference.InferType
import org.purescript.psi.PSPsiElement

class TypeString(node: ASTNode) : PSPsiElement(node), PSType {
    override val substitutedType: InferType get() = InferType.String
}