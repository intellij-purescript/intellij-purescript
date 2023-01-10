package org.purescript.psi.newtype

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import org.purescript.psi.name.PSProperName
import org.purescript.psi.base.PSPsiElement

/**
 * A constructor in a newtype declaration, e.g.
 *
 * ```
 * CatQueue (List a) (List a)
 * ```
 * in
 * ```
 * newtype CatQueue a = CatQueue (List a) (List a)
 * ```
 */
class PSNewTypeConstructor(node: ASTNode) :
    PSPsiElement(node),
    PsiNameIdentifierOwner {

    /**
     * @return the [PSProperName] identifying this constructor
     */
    internal val identifier: PSProperName
        get() = findNotNullChildByClass(PSProperName::class.java)

    override fun setName(name: String): PsiElement? = null

    override fun getNameIdentifier(): PSProperName = identifier

    override fun getName(): String = identifier.name
}
