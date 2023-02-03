package org.purescript.psi.expression

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.command.executeCommand
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.ProcessingContext
import org.purescript.file.PSFile
import org.purescript.psi.declaration.Importable
import org.purescript.psi.declaration.ImportableIndex
import org.purescript.psi.declaration.fixity.FixityDeclaration
import org.purescript.psi.declaration.value.ValueDeclarationGroup

class ImportableCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        val project = parameters.editor.project ?: return
        val index = ImportableIndex
        val scope = GlobalSearchScope.allScope(project)
        val names = index.getAllKeys(project)
        for (name in names) {
            if (result.isStopped) return
            if (!result.prefixMatcher.prefixMatches(name)) continue
            val elements = index.get(name, project, scope)
            val elementBuilders = elements
                .filter { parameters.originalFile != it.containingFile }
                .mapNotNull {
                    when (it) {
                        is ValueDeclarationGroup -> LookupElementBuilder
                            .create(it)
                            .withTypeText(it.signature?.type?.text)
                            .withTailText(it.module?.name?.let { "($it)" })
                            .withIcon(AllIcons.Nodes.Function)

                        is FixityDeclaration -> {
                            val reference = it.reference.resolve()
                                as? ValueDeclarationGroup
                            LookupElementBuilder.create(it)
                                .withTypeText(reference?.signature?.type?.text)
                                .withTailText(it.module?.name?.let { "($it)" })
                                .withIcon(AllIcons.Actions.Regex)
                        }
                        else -> null
                    }?.withInsertHandler { context, item ->
                        val import = (item.psiElement as? Importable)
                            ?.asImport() ?: return@withInsertHandler
                        val module = (context.file as PSFile).module
                        executeCommand(project, "Import") {
                            runWriteAction {
                                module?.addImportDeclaration(import)
                            }
                        }
                    }

                }
            result.addAllElements(elementBuilders)
        }
    }
}