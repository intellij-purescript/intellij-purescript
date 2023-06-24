package org.purescript.module.declaration.type.type

import com.intellij.lang.ASTNode
import org.purescript.inference.Scope
import org.purescript.inference.InferType
import org.purescript.psi.PSPsiElement

class TypeWildcard(node: ASTNode) : PSPsiElement(node), PSType {
    override fun infer(scope: Scope): InferType = scope.newUnknown()
}