package org.purescript.features

import com.intellij.psi.PsiComment

interface DocCommentOwner {
    val docComments: List<PsiComment>
}