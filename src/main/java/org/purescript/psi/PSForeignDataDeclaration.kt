package org.purescript.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import org.purescript.psi.typeconstructor.PSTypeConstructor

/**
 * A foreign data declaration, e.g.
 * ```
 * foreign import data Effect :: Type -> Type
 * ```
 */
class PSForeignDataDeclaration(node: ASTNode) :
        PSPsiElement(node),
        PsiNameIdentifierOwner {

    internal val typeConstructor: PSTypeConstructor
        get() = findNotNullChildByClass(PSTypeConstructor::class.java)

    override fun setName(name: String): PsiElement? =
            null

    // TODO This should maybe not be a PSTypeConstructor
    override fun getNameIdentifier(): PsiElement =
            typeConstructor

    override fun getName(): String =
            typeConstructor.name

    override fun getTextOffset(): Int =
            typeConstructor.textOffset
}
