package org.purescript.module.declaration.type.type

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import com.intellij.util.alsoIfNull
import org.purescript.inference.InferType

import org.purescript.psi.PSPsiElement

class PSConstrainedType(node: ASTNode) : PSPsiElement(node), PSType {
    val types get() = childrenOfType<PSType>()
    override fun unify() {
        val (constraint, of) = types
        val constraintType = constraint.inferType()
        (constraintType as? InferType.App)?.let { (f, union) ->
            (f as? InferType.App)?.let { (f, r) ->
                (f as? InferType.App)?.let { (unionName, l) ->
                    if (unionName == InferType.Union) {
                        unify(union, InferType.RowMerge(l as InferType.Row, r as InferType.Row))
                        of
                    } else null
                }
            }
        }
            ?.also { unify(it.inferType()) }
            .alsoIfNull { unify(InferType.Constraint(constraintType, of.inferType())) }
    }
}