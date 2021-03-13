package org.purescript.psi.data

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import org.purescript.psi.PSIdentifier
import org.purescript.psi.PSProperName
import org.purescript.psi.PSPsiElement
import org.purescript.psi.PSTypeAtomImpl

/**
 * A data constructor in a data declaration, e.g.
 *
 * ```
 * CatQueue (List a) (List a)
 * ```
 * in
 * ```
 * data CatQueue a = CatQueue (List a) (List a)
 * ```
 */
class PSDataConstructor(node: ASTNode) :
    PSPsiElement(node),
    PsiNameIdentifierOwner {

    /**
     * @return the [PSIdentifier] identifying this constructor
     */
    internal val identifier: PSIdentifier
        get() = findNotNullChildByClass(PSIdentifier::class.java)

    /**
     * @return the [PSTypeAtomImpl] elements in this constructor
     */
    internal val typeAtoms: Array<PSTypeAtomImpl>
        get() = findChildrenByClass(PSTypeAtomImpl::class.java)

    override fun setName(name: String): PsiElement? = null

    override fun getNameIdentifier(): PSIdentifier = identifier

    override fun getName(): String = identifier.name
}
