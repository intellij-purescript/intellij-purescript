package org.purescript.psi.expression

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.command.executeCommand
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.parentOfType
import com.intellij.util.ProcessingContext
import org.purescript.file.PSFile
import org.purescript.psi.declaration.Importable
import org.purescript.psi.declaration.ImportableIndex
import org.purescript.psi.declaration.fixity.FixityDeclaration
import org.purescript.psi.declaration.value.ValueDeclarationGroup
import org.purescript.psi.module.Module

class ImportableCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        if (!parameters.isExtendedCompletion) {
            result.addLookupAdvertisement("Only showing names in scope, complete again for more options")
            return
        }
        val localElement = parameters.position
        val qualifiedName = localElement.parentOfType<Qualified>()?.qualifierName
        val alreadyImportedModules: List<Module> = (localElement.containingFile as PSFile)
            .module
            ?.cache
            ?.importsByAlias
            ?.get(qualifiedName)
            ?.mapNotNull { it.importedModule }
            ?: emptyList()

        val importableDeclarations: Set<PsiElement> = if (parameters.invocationCount == 2) {
            result.addLookupAdvertisement("Showing names in already imported modules, complete again for project wide")
            alreadyImportedModules.flatMap {
                it.exportedValueDeclarationGroups + it.exportedFixityDeclarations
            }.toSet()
        } else emptySet()

        // import Module as Alias
        val project = parameters.editor.project ?: return
        val scope = GlobalSearchScope.allScope(project)
        val index = ImportableIndex
        val names = index.getAllKeys(project)
        for (name in names) {
            if (result.isStopped) return
            if (!result.prefixMatcher.prefixMatches(name)) continue
            val elements = index.get(name, project, scope)
            val elementBuilders = elements
                .filter { parameters.originalFile != it.containingFile }
                .filter { target ->
                    if (parameters.invocationCount < 3) {
                        target in importableDeclarations
                    } else {
                        true
                    }
                }
                .mapNotNull {
                    when (it) {
                        is ValueDeclarationGroup -> LookupElementBuilder
                            .createWithIcon(it)
                            .withTypeText(it.signature?.type?.text)
                            .withTailText(it.module?.name?.let { "($it)" })

                        is FixityDeclaration -> {
                            LookupElementBuilder.createWithIcon(it)
                                .withTypeText(it.signature?.type?.text)
                                .withTailText(it.module?.name?.let { "($it)" })
                        }

                        else -> null
                    }?.withInsertHandler { context, item ->
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
            result.addAllElements(elementBuilders)
        }
    }
}