package org.purescript.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import org.purescript.psi.name.PSProperName

/**
 * A foreign data declaration, e.g.
 * ```
 * foreign import data Effect :: Type -> Type
 * ```
 */
class PSForeignDataDeclaration(node: ASTNode) :
    PSPsiElement(node),
    PsiNameIdentifierOwner {

    internal val properName: PSProperName
        get() = findNotNullChildByClass(PSProperName::class.java)

    override fun setName(name: String): PsiElement? =
        null

    override fun getNameIdentifier(): PsiElement =
        properName

    override fun getName(): String =
        properName.name

    override fun getTextOffset(): Int =
        properName.textOffset
}
