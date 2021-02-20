package org.purescript.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.elementType
import org.purescript.features.DocCommentOwner
import org.purescript.parser.PSTokens

class PSForeignValueDeclaration(node: ASTNode) :
    PSPsiElement(node),
    PsiNameIdentifierOwner,
    DocCommentOwner
{
    override fun setName(name: String): PsiElement? {
        return null
    }

    override fun getNameIdentifier(): PSIdentifierImpl? {
        return findChildByClass(PSIdentifierImpl::class.java)
    }

    override fun getName(): String? {
        return nameIdentifier?.name
    }

    override val docComments: List<PsiComment>
        get() =
            generateSequence(prevSibling) { it.prevSibling }
                .dropWhile { it !is PsiComment && it !is DocCommentOwner }
                .takeWhile { it is PsiComment || it is PsiWhiteSpace }
                .filter { it.elementType == PSTokens.DOC_COMMENT}
                .filterIsInstance(PsiComment::class.java)
                .toList()
                .reversed()
}