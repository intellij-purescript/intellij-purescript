package org.purescript.features

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.lang.documentation.DocumentationMarkup.*
import com.intellij.openapi.editor.richcopy.HtmlSyntaxInfoUtil
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser
import org.purescript.PSLanguage
import org.purescript.inference.Inferable
import org.purescript.module.declaration.Importable
import org.purescript.module.declaration.classes.ClassDecl
import org.purescript.module.declaration.data.DataConstructor
import org.purescript.module.declaration.data.DataDeclaration
import org.purescript.module.declaration.value.ValueDeclarationGroup
import org.purescript.psi.PSPsiElement

class PSDocumentationProvider : AbstractDocumentationProvider() {

    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?) = when {
        element is Importable && element is DocCommentOwner ->
            layout(
                HtmlSyntaxInfoUtil.getHighlightedByLexerAndEncodedAsHtmlCodeSnippet(
                    element.project,
                    PSLanguage,
                    getType(element),
                    1f
                ),
                docCommentsToDocstring(element.docComments.map { it.text }, element.project)
            )

        element is DocCommentOwner && element is PsiNamedElement ->
            layout(
                element.name ?: "unknown",
                docCommentsToDocstring(element.docComments.map { it.text }, element.project)
            )

        else -> null
    }

    private fun getType(element: Importable): String? =
        (element.type?.text ?: try {
            (element as? Inferable)?.inferType()
        } catch (e: Exception) {
            null
        } catch (e: NotImplementedError) {
            null
        })?.let { "${element.name} :: $it" } ?: element.name

    override fun getUrlFor(element: PsiElement?, originalElement: PsiElement?) =
        pursuitUrlsFromSpagoPath(element)

    private fun pursuitUrlsFromSpagoPath(element: PsiElement?): MutableList<String> {
        val path = try {
            element?.containingFile?.virtualFile?.toNioPath()
                ?: return mutableListOf()
        } catch (e: Exception) {
            TODO("Not yet implemented")
        }
        val spagoPath = path
            .normalize()
            .map { it.fileName.toString() }
            .dropWhile { ".spago" != it }
            .drop(1)
            .toList()
        if (spagoPath.size < 2) {
            return mutableListOf()
        }
        val (packageName, rawVersion) = spagoPath
        val version = rawVersion.trimStart('v')

        return when (element) {
            is ValueDeclarationGroup ->
                mutableListOf("https://pursuit.purescript.org/packages/purescript-$packageName/${version}/docs/${element.module.name}#v:${element.name}")

            is DataConstructor ->
                mutableListOf("https://pursuit.purescript.org/packages/purescript-$packageName/${version}/docs/${element.module.name}#v:${element.name}")

            is DataDeclaration ->
                mutableListOf("https://pursuit.purescript.org/packages/purescript-$packageName/${version}/docs/${element.module.name}#t:${element.name}")

            is ClassDecl ->
                mutableListOf("https://pursuit.purescript.org/packages/purescript-$packageName/${version}/docs/${element.module.name}#t:${element.name}")

            is org.purescript.module.Module ->
                mutableListOf("https://pursuit.purescript.org/packages/purescript-$packageName/${version}/docs/${element.name}")

            is PSPsiElement ->
                mutableListOf("https://pursuit.purescript.org/packages/purescript-$packageName/${version}/docs/${element.module.name}")

            else -> mutableListOf()
        }
    }

    fun docCommentsToDocstring(commentText: List<String>, project: Project): String {
        val lines = commentText
            .joinToString("\n") { it.trim().removePrefix("-- |") }
            .trimIndent()
        val flavour = GFMFlavourDescriptor()
        val parsedTree = MarkdownParser(flavour).buildMarkdownTreeFromString(lines)
        val html = HtmlGenerator(lines, parsedTree, flavour).generateHtml()
        return html.replace("\n", "<br />").trim()
    }

    sealed interface MState {
        fun process(line: String): MState
        fun text(): String
        data class Normal(val project: Project, val out: String) : MState {
            override fun text(): String = out
            override fun process(line: String): MState {
                return if (line.startsWith("```")) {
                    CodeBlock(project, out, "")
                } else {
                    val formatted = line.replace(Regex("`([^`]*)`")) {
                        HtmlSyntaxInfoUtil.getHighlightedByLexerAndEncodedAsHtmlCodeSnippet(
                            project,
                            PSLanguage,
                            it.value,
                            1f
                        )
                    }.let { "<code>$it</code>" }
                    Normal(project, "$out\n$formatted")
                }
            }
        }

        data class CodeBlock(val project: Project, val out: String, val code: String) : MState {
            override fun text(): String = out
            override fun process(line: String): MState {
                return if (line.startsWith("```")) {
                    val formatted = HtmlSyntaxInfoUtil.getHighlightedByLexerAndEncodedAsHtmlCodeSnippet(
                        project,
                        PSLanguage,
                        code,
                        1f
                    ).let { "<code>$it</code>" }
                    Normal(project, "$out\n$formatted")
                } else {
                    CodeBlock(project, out, "$code\n$line")
                }
            }
        }
    }

    fun layout(definition: String, mainDescription: String) =
        DEFINITION_START + definition + DEFINITION_END +
                CONTENT_START + mainDescription + CONTENT_END
}
