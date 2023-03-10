package org.purescript.psi.type

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.command.executeCommand
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.parentOfType
import com.intellij.util.ProcessingContext
import org.purescript.file.PSFile
import org.purescript.psi.declaration.Importable
import org.purescript.psi.declaration.ImportableTypeIndex
import org.purescript.psi.expression.Qualified

class ImportableTypeCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        val localElement = parameters.position
        val qualifiedName = localElement.parentOfType<Qualified>()?.qualifierName
        // import Module as Alias
        val project = parameters.editor.project ?: return
        val scope = GlobalSearchScope.allScope(project)
        val index = ImportableTypeIndex
        val names = index.getAllKeys(project)
        for (name in names) {
            if (result.isStopped) return
            if (!result.prefixMatcher.prefixMatches(name)) continue
            val elements = index.get(name, project, scope)
            val elementBuilders = elements
                .filter { parameters.originalFile != it.containingFile }
                .mapNotNull { lookupElementBuilder(it, qualifiedName, project) }
            result.addAllElements(elementBuilders)
        }
    }

    private fun lookupElementBuilder(
        it: Importable,
        qualifiedName: String?,
        project: Project
    ) = LookupElementBuilder
        .createWithIcon(it)
        .withTypeText(it.type?.text)
        .withTailText(it.asImport()?.moduleName?.let { "($it)" })
        .withInsertHandler { context, item ->
            val import = (item.psiElement as? Importable)
                ?.asImport()
                ?.withAlias(qualifiedName)
                ?: return@withInsertHandler
            val module = (context.file as PSFile).module
            executeCommand(project, "Import") {
                runWriteAction {
                    module?.addImportDeclaration(import)
                }
            }
        }
}