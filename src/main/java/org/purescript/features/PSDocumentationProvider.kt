package org.purescript.features

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import org.purescript.psi.PSModule
import org.purescript.psi.PSValueDeclaration

class PSDocumentationProvider: AbstractDocumentationProvider() {
    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
        return when (element) {
            is PSModule -> {
                docCommentsToDocstring(element.docComments.map {it.text})
            }
            is PSValueDeclaration -> {
                docCommentsToDocstring(element.docComments.map {it.text})
            }
            else -> null
        }
    }

    fun docCommentsToDocstring(commentText: List<String>) =
        commentText
            .map { it.trim() }
            .map { it.removePrefix("-- |") }
            .map {
                if (it.isBlank()) {
                    "<br/><br/>"
                } else {
                    it
                }
            }
            .joinToString(" ") { it.trim() }
}
