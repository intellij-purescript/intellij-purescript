package org.purescript.module.declaration.type

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.command.executeCommand
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.util.parentOfType
import com.intellij.util.ProcessingContext
import org.purescript.PSLanguage
import org.purescript.PrimTypePsiElement
import org.purescript.file.PSFile
import org.purescript.module.declaration.Importable
import org.purescript.module.declaration.ImportableTypeIndex
import org.purescript.module.declaration.value.expression.Qualified

class ImportableTypeCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        val localElement = parameters.position
        val qualifierName = localElement.parentOfType<Qualified>()?.qualifierName
        // import Module as Alias
        val project = parameters.editor.project ?: return
        val scope = GlobalSearchScope.allScope(project)
        val index = ImportableTypeIndex
        val names = index.getAllKeys(project)
        for (name in names) {
            if (result.isStopped) return
            if (!result.prefixMatcher.prefixMatches(name)) continue
            val elements = StubIndex.getElements(
                index.key,
                name,
                project,
                scope,
                Importable::class.java
            )
            val elementBuilders = elements
                .filter { parameters.originalFile != it.containingFile }
                .mapNotNull { lookupElementBuilder(it, qualifierName, project) }
            result.addAllElements(elementBuilders)
        }

        for (entry in PSLanguage.BUILTIN_MODULES_MAP) {
            if (entry.key == "Prim") {
                PSLanguage.getPrimModule(project)?.exportedTypes?.forEach { type ->
                    result.addElement(
                        LookupElementBuilder
                            .createWithIcon(type)
                            .withTailText("(Prim)")
                    )
                }
            } else for (element in PSLanguage.getBuiltins(project, entry.key)) {
                if (result.isStopped) return
                if (!result.prefixMatcher.prefixMatches(element.name)) continue
                val elementBuilder = lookupElementBuilderForPrim(element, entry, qualifierName, project)
                result.addElement(elementBuilder)

            }
        }
    }

    private fun lookupElementBuilderForPrim(
        element: PrimTypePsiElement,
        entry: Map.Entry<String, List<String>>,
        qualifierName: String?,
        project: Project
    ) =
        LookupElementBuilder
            .create(element.name)
            .withTailText("(${entry.key})")
            .withInsertHandler { context, item ->
                val import = element.asImport().withAlias(qualifierName)
                val module = (context.file as PSFile).module
                executeCommand(project, "Import") {
                    runWriteAction<Unit> {
                        module?.addImportDeclaration(import)
                    }
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