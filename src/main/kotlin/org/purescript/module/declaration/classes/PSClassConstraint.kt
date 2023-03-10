package org.purescript.module.declaration.classes

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiReference
import org.purescript.psi.PSPsiElement
import org.purescript.module.declaration.type.PSTypeAtom
import org.purescript.name.PSClassName

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
     * @return the [PSTypeAtom] elements that this constraint contains,
     * or an empty array if it does not contain any.
     */
    val typeAtoms: Array<PSTypeAtom>
        get() = findChildrenByClass(PSTypeAtom::class.java)

    override fun getName(): String = identifier.name

    override fun getReference(): PsiReference =
        ClassConstraintReference(this)
}
