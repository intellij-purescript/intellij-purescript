package org.purescript.module.declaration.type.type

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.inference.InferType

import org.purescript.psi.PSPsiElement

class PSConstrainedType(node: ASTNode) : PSPsiElement(node), PSType {
    val types get() = childrenOfType<PSType>()
    override fun unify() {
        val (constraint, of) = types
        unify(InferType.Constraint(constraint.inferType(), of.inferType()))
    }
}