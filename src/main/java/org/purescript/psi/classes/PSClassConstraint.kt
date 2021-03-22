package org.purescript.psi.classes

import com.intellij.lang.ASTNode
import org.purescript.psi.PSProperName
import org.purescript.psi.PSPsiElement
import org.purescript.psi.PSTypeAtomImpl

/**
 * An individual constraint in a class declaration, e.g.
 * ```
 * Semigroup m
 * ```
 * in
 * ```
 * class Semigroup m <= Monoid m
 * ```
 */
class PSClassConstraint(node: ASTNode) : PSPsiElement(node) {

    /**
     * @return the [PSProperName] that identifies this constraint
     */
    val identifier: PSProperName
        get() = findNotNullChildByClass(PSProperName::class.java)

    /**
     * @return the [PSTypeAtomImpl] elements that this constraint contains,
     * or an empty array if it does not contain any.
     */
    val typeAtoms: Array<PSTypeAtomImpl>
        get() = findChildrenByClass(PSTypeAtomImpl::class.java)

    override fun getName(): String = identifier.name
}
