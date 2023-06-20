package org.purescript.module.declaration.type.type

import com.intellij.lang.ASTNode
import org.purescript.inference.Scope
import org.purescript.inference.Type
import org.purescript.module.declaration.type.Labeled
import org.purescript.psi.PSPsiElement

class TypeRecord(node: ASTNode) : PSPsiElement(node), PSType {
    private val labels get() = findChildrenByClass(Labeled::class.java)
    override fun infer(scope: Scope): Type {
        return Type.Record.app(Type.Row(labels.map { 
            it.name to (it.type?.infer(scope) ?: scope.newUnknown()) 
        }))
    }
}