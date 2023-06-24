package org.purescript.module.declaration.type.type

import com.intellij.lang.ASTNode
import org.purescript.inference.InferType
import org.purescript.inference.Scope
import org.purescript.inference.Unifiable
import org.purescript.inference.unifyAndSubstitute
import org.purescript.module.declaration.type.Labeled
import org.purescript.psi.PSPsiElement

class TypeRecord(node: ASTNode) : PSPsiElement(node), PSType, Unifiable {
    private val labels get() = findChildrenByClass(Labeled::class.java)
    override fun infer(scope: Scope): InferType {
        return InferType.Record.app(InferType.Row(labels.map { 
            it.name to (it.type?.infer(scope) ?: scope.newUnknown()) 
        }))
    }

    override fun unify() {
        unify(InferType.Record.app(InferType.Row(labels.mapNotNull {
            it.name to (it.type?.unifyAndSubstitute() ?: error("$it.name did not have a type") )
        })))
    }
}