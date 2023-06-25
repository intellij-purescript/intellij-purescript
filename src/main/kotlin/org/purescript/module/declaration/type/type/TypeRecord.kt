package org.purescript.module.declaration.type.type

import com.intellij.lang.ASTNode
import org.purescript.inference.InferType
import org.purescript.inference.Unifiable

import org.purescript.module.declaration.type.Labeled
import org.purescript.psi.PSPsiElement

class TypeRecord(node: ASTNode) : PSPsiElement(node), PSType, Unifiable {
    private val labels get() = findChildrenByClass(Labeled::class.java)
    override fun unify() {
        unify(InferType.Record.app(InferType.RowList(labels.mapNotNull {
            it.name to (it.type?.inferType() ?: error("$it.name did not have a type") )
        })))
    }
}