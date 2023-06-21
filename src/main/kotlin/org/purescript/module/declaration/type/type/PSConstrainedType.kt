package org.purescript.module.declaration.type.type

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.inference.Scope
import org.purescript.inference.Type
import org.purescript.psi.PSPsiElement

class PSConstrainedType(node: ASTNode) : PSPsiElement(node), PSType {
    val types get() = childrenOfType<PSType>()
    override fun infer(scope: Scope): Type {
        val (constraint, of) = types
        return Type.Constraint( constraint.infer(scope), of.infer(scope))
    }
}