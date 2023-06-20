package org.purescript.module.declaration.value.expression

import com.intellij.lang.ASTNode
import org.purescript.inference.Scope
import org.purescript.inference.Type
import org.purescript.psi.PSPsiElement

class RecordAccess(node: ASTNode) : PSPsiElement(node), Expression {
    // TODO: fix this
    override fun infer(scope: Scope): Type = scope.newUnknown()
}