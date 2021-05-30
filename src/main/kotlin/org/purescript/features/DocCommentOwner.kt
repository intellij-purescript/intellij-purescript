package org.purescript.features

import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.elementType
import org.purescript.parser.PSTokens

interface DocCommentOwner {
    val docComments: List<PsiComment>


    fun PsiElement.getDocComments(): List<PsiComment> =
        generateSequence(prevSibling) { it.prevSibling }
            .dropWhile { it !is PsiComment && it !is DocCommentOwner }
            .takeWhile { it is PsiComment || it is PsiWhiteSpace }
            .filter { it.elementType == PSTokens.DOC_COMMENT }
            .filterIsInstance(PsiComment::class.java)
            .toList()
            .reversed()
}