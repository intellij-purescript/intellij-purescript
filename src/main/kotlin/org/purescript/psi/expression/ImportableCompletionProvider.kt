package org.purescript.psi.expression

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.util.ExecUtil
import com.intellij.icons.AllIcons
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.command.executeCommand
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.runBackgroundableTask
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.parentOfType
import com.intellij.util.ProcessingContext
import org.purescript.PackageSet
import org.purescript.file.PSFile
import org.purescript.ide.formatting.ImportDeclaration
import org.purescript.ide.formatting.ImportedValue
import org.purescript.psi.declaration.Importable
import org.purescript.psi.declaration.ImportableIndex
import org.purescript.psi.module.Module
import org.purescript.run.spago.Spago

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
        if (parameters.invocationCount >= 3) {
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
                        .withInsertHandler { context, item ->
                            val commandLine = project.service<Spago>().commandLine
                                .withParameters("install", packageName)
                            val import = ImportDeclaration(moduleName)
                                .withItems(ImportedValue(name))
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
            .flatMap {
                it.exportedValueDeclarationGroups +
                        it.exportedForeignValueDeclarations +
                        it.exportedDataConstructors +
                        it.exportedFixityDeclarations
            }
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