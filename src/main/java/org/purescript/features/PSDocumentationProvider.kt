package org.purescript.features

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.psi.PsiElement
import com.petebevin.markdown.MarkdownProcessor
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

    fun docCommentsToDocstring(commentText: List<String>): String {
        val processor = MarkdownProcessor()
        val markdown = commentText
            .map { it.trim() }
            .map { it.removePrefix("-- |") }
            .joinToString("\n") { it.trim() }
        return processor.markdown(markdown).trim()
    }
}
