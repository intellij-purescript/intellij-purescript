package org.purescript.features

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.psi.PsiElement
import org.purescript.psi.PSModule
import org.purescript.psi.PSValueDeclaration

class PurescriptDocumentationProvider: AbstractDocumentationProvider() {
    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
        return when (element) {
            is PSModule -> {
                docCommentsToDocstring(element.docComments)
            }
            is PSValueDeclaration -> {
                docCommentsToDocstring(element.docComments)
            }
            else -> null
        }
    }

    private fun docCommentsToDocstring(docComments: List<PsiElement>) =
        docComments.asSequence()
            .map { it.text }
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
