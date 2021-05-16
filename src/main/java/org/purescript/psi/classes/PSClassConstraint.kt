package org.purescript.psi.classes

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiReference
import org.purescript.psi.name.PSProperName
import org.purescript.psi.PSPsiElement
import org.purescript.psi.PSTypeAtomImpl
import org.purescript.psi.name.PSClassName

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
     * @return the [PSClassName] that identifies this constraint
     */
    val identifier: PSClassName
        get() = findNotNullChildByClass(PSClassName::class.java)

    /**
     * @return the [PSTypeAtomImpl] elements that this constraint contains,
     * or an empty array if it does not contain any.
     */
    val typeAtoms: Array<PSTypeAtomImpl>
        get() = findChildrenByClass(PSTypeAtomImpl::class.java)

    override fun getName(): String = identifier.name

    override fun getReference(): PsiReference =
        ClassConstraintReference(this)
}
