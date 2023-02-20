package org.purescript.psi.expression

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionSorter
import com.intellij.codeInsight.completion.PrefixMatchingWeigher
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.codeInsight.lookup.LookupElementWeigher
import com.intellij.codeInsight.lookup.WeighingContext
import com.intellij.execution.util.ExecUtil
import com.intellij.icons.AllIcons
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.command.executeCommand
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.runBackgroundableTask
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.parentOfType
import com.intellij.util.ProcessingContext
import org.purescript.PackageSet
import org.purescript.file.PSFile
import org.purescript.ide.formatting.ImportDeclaration
import org.purescript.ide.formatting.ImportedOperator
import org.purescript.ide.formatting.ImportedValue
import org.purescript.psi.declaration.Importable
import org.purescript.psi.declaration.ImportableIndex
import org.purescript.run.spago.Spago

class ImportableCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        if (parameters.isExtendedCompletion) {
            addInstallCompletions(parameters, result)
        }
        completionsFromIndex(parameters, result)
    }

    private fun addInstallCompletions(parameters: CompletionParameters, result: CompletionResultSet) {
        val localElement = parameters.position
        val qualifiedName = localElement.parentOfType<Qualified>()?.qualifierName
        val project = parameters.editor.project ?: return
        val packageSet = project.service<PackageSet>()
        for ((name, data) in packageSet.reverseLookup) {
            if (result.isStopped) return
            if (!result.prefixMatcher.prefixMatches(name)) continue
            for ((packageName, moduleName) in data) {
                result.addElement(LookupElementBuilder
                    .create(packageName to moduleName, name)
                    .withIcon(AllIcons.Actions.Install)
                    .withTailText("($moduleName)")
                    .appendTailText("($packageName)", true)
                    .withInsertHandler { context, _ ->
                        val commandLine = project.service<Spago>().commandLine
                            .withParameters("install", packageName)
                        val import = ImportDeclaration(moduleName)
                            .let {
                                when {
                                    name.firstOrNull()?.isLetter() == true ->
                                        it.withItems(ImportedValue(name))
                                    else -> it.withItems(ImportedOperator(name))
                                }
                            }
                            .withAlias(qualifiedName)
                        val module = (context.file as PSFile).module
                        runBackgroundableTask("Installing package: $packageName", project) {
                            ExecUtil.execAndGetOutput(commandLine)
                            VirtualFileManager.getInstance().asyncRefresh {}
                        }
                        executeCommand(project, "Import") {
                            runWriteAction {
                                module?.addImportDeclaration(import)
                            }
                        }
                    })
            }
        }
    }

    private fun completionsFromIndex(parameters: CompletionParameters, result: CompletionResultSet) {
        val localElement = parameters.position
        val qualifiedName = localElement.parentOfType<Qualified>()?.qualifierName
        val improvedResult = if (qualifiedName != null) {
            val qualifiedMather =  result.prefixMatcher.cloneWithPrefix(qualifiedName)
            result
                .withRelevanceSorter(CompletionSorter.emptySorter().weigh(object : LookupElementWeigher("qualifier") {
                    override fun weigh(element: LookupElement): Comparable<Nothing> {
                        return - element.allLookupStrings.count {qualifiedMather.prefixMatches(it)}
                    }
                }))
        } else {
            result
        }
        // import Module as Alias
        val project = parameters.editor.project ?: return
        val scope = GlobalSearchScope.allScope(project)
        val index = ImportableIndex
        val names = index.getAllKeys(project)
        for (name in names) {
            if (improvedResult.isStopped) return
            if (!improvedResult.prefixMatcher.prefixMatches(name)) continue
            val elements = index.get(name, project, scope)
            val elementBuilders = elements
                .filter { parameters.originalFile != it.containingFile }
                .mapNotNull { lookupElementBuilder(it, qualifiedName, project) }
            improvedResult.addAllElements(elementBuilders)
        }
    }

    private fun lookupElementBuilder(
        it: Importable,
        qualifiedName: String?,
        project: Project
    ): LookupElementBuilder {
        val import = it.asImport()?.withAlias(qualifiedName) 
            ?: error("Importable was not importable")
        val modulePath = import.moduleName.split('.')
        return LookupElementBuilder
            .createWithIcon(it)
            .withPresentableText(it.name!!)
            .withLookupString("${modulePath.joinToString("")}.${it.name}")
            .withLookupString("${modulePath.reversed().joinToString("")}.${it.name}")
            .withTypeText(it.type?.text)
            .withTailText("(${import.moduleName})")
            .withInsertHandler { context, item ->
                val module = (context.file as PSFile).module
                executeCommand(project, "Import") {
                    runWriteAction {
                        module?.addImportDeclaration(import)
                    }
                }
            }
    }
}