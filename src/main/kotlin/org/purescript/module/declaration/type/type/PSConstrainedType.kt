package org.purescript.module.declaration.type.type

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.inference.InferType
import org.purescript.inference.Scope
import org.purescript.inference.unifyAndSubstitute
import org.purescript.psi.PSPsiElement

class PSConstrainedType(node: ASTNode) : PSPsiElement(node), PSType {
    val types get() = childrenOfType<PSType>()
    override fun infer(scope: Scope): InferType {
        val (constraint, of) = types
        return InferType.Constraint( constraint.infer(scope), of.infer(scope))
    }

    override fun unify() {
        val (constraint, of) = types
        val constraintType = constraint.unifyAndSubstitute()
        val onType = of.unifyAndSubstitute()
        unify(InferType.Constraint(constraintType, onType))
    }
}