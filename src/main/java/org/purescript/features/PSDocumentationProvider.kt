package org.purescript.features

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.lang.documentation.DocumentationMarkup.*
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.util.text.MarkdownUtil.replaceCodeBlock
import com.petebevin.markdown.MarkdownProcessor

class PSDocumentationProvider : AbstractDocumentationProvider() {
    override fun generateDoc(
        element: PsiElement?,
        originalElement: PsiElement?
    ): String? {
        if (element is DocCommentOwner && element is PsiNamedElement) {
            return layout(
                element.name?: "unknown",
                docCommentsToDocstring(element.docComments.map { it.text })
            )
        }
        return null
    }

    fun docCommentsToDocstring(commentText: List<String>): String {
        val processor = MarkdownProcessor()
        val lines = commentText
            .joinToString("\n") { it.trim().removePrefix("-- |") }
            .trimIndent()
            .lines()
            .toMutableList()

        replaceCodeBlock(lines)

        val markdown = lines.joinToString("\n")

        return processor.markdown(markdown).trim()
    }

    fun layout(definition: String, mainDescription: String): String {
        return DEFINITION_START + definition + DEFINITION_END +
            CONTENT_START + mainDescription + CONTENT_END
    }
}
