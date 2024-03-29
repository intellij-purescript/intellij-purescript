package org.purescript.module.declaration.type.type

import com.intellij.lang.ASTNode
import org.purescript.inference.InferType

import org.purescript.module.declaration.type.Labeled
import org.purescript.psi.PSPsiElement

class TypeRow(node: ASTNode) : PSPsiElement(node), PSType {
    private val labels get() = findChildrenByClass(Labeled::class.java)
    override fun unify() {
        unify(InferType.RowList(labels.map { it.name to it.inferType() }))
    }
}