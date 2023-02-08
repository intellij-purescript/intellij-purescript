package org.purescript.psi.expression

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.command.executeCommand
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.parentOfType
import com.intellij.util.ProcessingContext
import org.purescript.file.PSFile
import org.purescript.psi.declaration.Importable
import org.purescript.psi.declaration.ImportableIndex
import org.purescript.psi.module.Module

class ImportableCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        if (parameters.isExtendedCompletion) {
            addCompletions2(parameters, context, result)
        } else {
            addCompletions1(parameters, context, result)
        }
    }

    fun addCompletions1(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        result.addLookupAdvertisement("Showing names in already imported modules, complete again for project wide")
        val localElement = parameters.position
        val qualifiedName = localElement.parentOfType<Qualified>()?.qualifierName
        val alreadyImportedModules: List<Module> = (localElement.containingFile as PSFile)
            .module
            ?.cache
            ?.importsByAlias
            ?.get(qualifiedName)
            ?.mapNotNull { it.importedModule }
            ?: emptyList()
        val importableDeclarations: Set<PsiElement> = alreadyImportedModules
            .flatMap { it.exportedValueDeclarationGroups + it.exportedFixityDeclarations }
            .toSet()

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
                .filter { target -> target in importableDeclarations }
                .mapNotNull { lookupElementBuilder(it, qualifiedName, project) }
            result.addAllElements(elementBuilders)
        }
    }

    fun addCompletions2(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        val localElement = parameters.position
        val qualifiedName = localElement.parentOfType<Qualified>()?.qualifierName
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