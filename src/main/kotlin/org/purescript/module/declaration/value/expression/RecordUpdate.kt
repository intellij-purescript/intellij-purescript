package org.purescript.module.declaration.value.expression

import com.intellij.lang.ASTNode
import org.purescript.inference.Scope
import org.purescript.inference.Type
import org.purescript.psi.PSPsiElement

class RecordUpdate(node: ASTNode) : PSPsiElement(node), Expression {
    override fun infer(scope: Scope): Type = TODO("Not yet implemented")
}