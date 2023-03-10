package org.purescript.module.declaration.classes

import com.intellij.lang.ASTNode
import org.purescript.psi.PSPsiElement

/**
 * The class constraint portion of a class declaration, e.g.
 * ```
 * (Decide f, Divisible f) <=
 * ```
 * in
 * ```
 * class (Decide f, Divisible f) <= Decidable f
 * ```
 */
class PSClassConstraintList(node: ASTNode) : PSPsiElement(node) {

    /**
     * @return the [PSClassConstraint] elements in this list
     */
    val classConstraints: Array<PSClassConstraint>
        get() = findChildrenByClass(PSClassConstraint::class.java)
}
