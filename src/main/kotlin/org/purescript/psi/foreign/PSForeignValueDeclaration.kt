package org.purescript.psi.foreign

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import org.purescript.features.DocCommentOwner
import org.purescript.psi.base.PSPsiElement
import org.purescript.psi.name.PSIdentifier

/**
 * A foreign value import declaration, e.g.
 *
 * ```
 * foreign import xor :: Int -> Int -> Int
 * ```
 */
class PSForeignValueDeclaration(node: ASTNode) :
    PSPsiElement(node),
    PsiNameIdentifierOwner,
    DocCommentOwner
{
    override fun setName(name: String): PsiElement? {
        return null
    }

    override fun getNameIdentifier(): PSIdentifier? {
        return findChildByClass(PSIdentifier::class.java)
    }

    override fun getName(): String? {
        return nameIdentifier?.name
    }

    override val docComments: List<PsiComment>
        get() = this.getDocComments()
}
