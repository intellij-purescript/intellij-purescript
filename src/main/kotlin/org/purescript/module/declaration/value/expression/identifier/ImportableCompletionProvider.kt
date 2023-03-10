package org.purescript.module.declaration.value.expression.identifier

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.codeInsight.lookup.LookupElementWeigher
import com.intellij.execution.util.ExecUtil
import com.intellij.icons.AllIcons
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.command.executeCommand
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.runBackgroundableTask
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.parentOfType
import com.intellij.util.ProcessingContext
import com.intellij.util.io.isAncestor
import org.purescript.PackageSet
import org.purescript.file.PSFile
import org.purescript.module.declaration.ImportableIndex
import org.purescript.module.declaration.value.expression.Qualified
import org.purescript.psi.PSPsiElement
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
        completionsOfNamespace(parameters, result)
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
            for ((packageName, import) in data) {
                val moduleName = import.moduleName
                result.addElement(LookupElementBuilder
                    .create(packageName to moduleName, name)
                    .withIcon(AllIcons.Actions.Install)
                    .withTailText("($moduleName)")
                    .appendTailText("($packageName)", true)
                    .withInsertHandler { context, _ ->
                        val commandLine = project.service<Spago>().commandLine
                            .withParameters("install", packageName)
                        val qualifiedImport = import.withAlias(qualifiedName)
                        val module = (context.file as PSFile).module
                        runBackgroundableTask("Installing package: $packageName", project) {
                            ExecUtil.execAndGetOutput(commandLine)
                            VirtualFileManager.getInstance().asyncRefresh {}
                        }
                        executeCommand(project, "Import") {
                            runWriteAction {
                                module?.addImportDeclaration(qualifiedImport)
                            }
                        }
                    })
            }
        }
    }

    private fun completionsOfNamespace(parameters: CompletionParameters, result: CompletionResultSet) {
        val localElement = parameters.position.parentOfType<Qualified>(true) ?: return
        if ((localElement as? Qualified)?.qualifierName != null) return
        val module = (localElement  as? PSPsiElement)?.module ?: return
        val nameSpaces = module.cache.importsByAlias.keys.filterNotNull()
        result.addAllElements(
            nameSpaces
                .filter { result.prefixMatcher.prefixMatches(it) }
                .map {
                LookupElementBuilder.create("$it.")
            }
        )
        for (nameSpace in nameSpaces) {
            val imports = module.cache.importsByAlias[nameSpace] ?: continue
            for (import in imports) {
                val items = import.importedItems
                    .mapNotNull { it.reference?.resolve() }
                    .filterIsInstance<PsiNamedElement>()
                result.addAllElements(
                    items.map { LookupElementBuilder
                        .create(it, "$nameSpace.${it.name}")
                        .withIcon(it.getIcon(0))
                        .withTypeText((it as? org.purescript.module.declaration.Importable)?.type?.text)
                        .withTailText("(${import.moduleName.name})")
                        .let { PrioritizedLookupElement.withPriority(it, 1.0) }
                    }
                )
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

    private fun lookupElementBuilder(it: org.purescript.module.declaration.Importable, qualifiedName: String?, project: Project): LookupElementBuilder {
        val import = it.asImport()?.withAlias(qualifiedName) 
            ?: error("Importable was not importable")
        val modulePath = import.moduleName.split('.')

        val lookupElement = LookupElementBuilder
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
        val file = it.containingFile.virtualFile
        val libraries = project.service<Spago>().libraries
        return libraries.find {
            it.sourceRoots.any { it.toNioPath().isAncestor(file.toNioPath()) }
        }?.packageName?.let { lookupElement.appendTailText("($it)", true) }
            ?: lookupElement
    }
}