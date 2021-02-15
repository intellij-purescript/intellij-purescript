package org.purescript.features

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.lang.documentation.DocumentationMarkup.*
import com.intellij.psi.PsiElement
import com.intellij.util.text.MarkdownUtil.replaceCodeBlock
import com.petebevin.markdown.MarkdownProcessor
import org.purescript.psi.PSModule
import org.purescript.psi.PSValueDeclaration

class PSDocumentationProvider : AbstractDocumentationProvider() {
    override fun generateDoc(
        element: PsiElement?,
        originalElement: PsiElement?
    ): String? {
        return when (element) {
            is PSModule -> {
                layout(
                    element.name,
                    docCommentsToDocstring(element.docComments.map { it.text })
                )
            }
            is PSValueDeclaration -> {
                layout(
                    element.name,
                    docCommentsToDocstring(element.docComments.map { it.text })
                )
            }
            else -> null
        }
    }

    fun docCommentsToDocstring(commentText: List<String>): String {
        val processor = MarkdownProcessor()
        val lines = commentText
            .map { it.trim() }
            .map { it.removePrefix("-- |").trim() }
            .toMutableList()
        replaceCodeBlock(lines)
        val markdown = lines.joinToString("\n") { it.trim() }
        return processor.markdown(markdown).trim()
    }

    fun layout(definition: String, mainDescription: String): String {
        return DEFINITION_START + definition + DEFINITION_END +
            CONTENT_START + mainDescription + CONTENT_END
    }
}
