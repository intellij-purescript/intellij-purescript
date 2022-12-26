package org.purescript.features

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.lang.documentation.DocumentationMarkup.*
import com.intellij.openapi.editor.richcopy.HtmlSyntaxInfoUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.util.text.MarkdownUtil.replaceCodeBlock
import com.petebevin.markdown.MarkdownProcessor
import org.purescript.PSLanguage
import org.purescript.psi.module.PSModule
import org.purescript.psi.PSPsiElement
import org.purescript.psi.classes.PSClassDeclaration
import org.purescript.psi.data.PSDataConstructor
import org.purescript.psi.data.PSDataDeclaration
import org.purescript.psi.declaration.PSValueDeclaration

class PSDocumentationProvider : AbstractDocumentationProvider() {
    override fun generateDoc(
        element: PsiElement?,
        originalElement: PsiElement?
    ): String? {
        return when  {
            element is PSValueDeclaration ->
                layout(
                    HtmlSyntaxInfoUtil.getHighlightedByLexerAndEncodedAsHtmlCodeSnippet(
                        element.project,
                        PSLanguage.INSTANCE,
                        element.signature?.text ?: element.name,
                        1f
                    ) ,
                    docCommentsToDocstring(element.docComments.map { it.text })
                )
            element is DocCommentOwner && element is PsiNamedElement ->
                layout(
                    element.name ?: "unknown",
                    docCommentsToDocstring(
                        element.docComments.map { it.text }
                    )
                )

            else -> null
        }
    }

    override fun getUrlFor(element: PsiElement?, originalElement: PsiElement?) =
        pursuitUrlsFromSpagoPath(element)

    private fun pursuitUrlsFromSpagoPath(element: PsiElement?): MutableList<String> {
        val path = element?.containingFile?.virtualFile?.toNioPath()
            ?: return mutableListOf()
        val spagoPath = path
            .normalize()
            .map { it.fileName.toString() }
            .dropWhile { !".spago".equals(it) }
            .drop(1)
            .toList()
        if (spagoPath.size < 2) {
            return mutableListOf()
        }
        val (packageName, rawVersion) = spagoPath
        val version = rawVersion.trimStart('v')

        return when (element) {
            is PSValueDeclaration ->
                mutableListOf("https://pursuit.purescript.org/packages/purescript-$packageName/${version}/docs/${element.module?.name}#v:${element.name}")

            is PSDataConstructor ->
                mutableListOf("https://pursuit.purescript.org/packages/purescript-$packageName/${version}/docs/${element.module?.name}#v:${element.name}")

            is PSDataDeclaration ->
                mutableListOf("https://pursuit.purescript.org/packages/purescript-$packageName/${version}/docs/${element.module?.name}#t:${element.name}")

            is PSClassDeclaration ->
                mutableListOf("https://pursuit.purescript.org/packages/purescript-$packageName/${version}/docs/${element.module?.name}#t:${element.name}")

            is PSModule ->
                mutableListOf("https://pursuit.purescript.org/packages/purescript-$packageName/${version}/docs/${element.name}")

            is PSPsiElement ->
                mutableListOf("https://pursuit.purescript.org/packages/purescript-$packageName/${version}/docs/${element.module?.name}")

            else -> mutableListOf()
        }
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
