package org.purescript.psi.expression

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.ProcessingContext
import org.purescript.psi.declaration.value.ExportedValueDeclNameIndex

class ExpressionIdentifierCompletionContributor : CompletionContributor() {
    init {
        val expressionIdentifier = psiElement()
            .withSuperParent(3, PSExpressionIdentifier::class.java)
        extend(CompletionType.BASIC, expressionIdentifier, Provider())
    }
    

    class Provider : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            result: CompletionResultSet
        ) {
            val project = parameters.editor.project ?: return
            val index = ExportedValueDeclNameIndex
            val scope = GlobalSearchScope.allScope(project)
            
            val names = index.getAllKeys(project)
            for (name in names) {
                if(result.isStopped) return
                if(!result.prefixMatcher.prefixMatches(name)) continue
                val elements = index.get(name, project, scope)
                val elementBuilders = elements
                    .filter { parameters.originalFile != it.containingFile }
                    .map {
                    LookupElementBuilder.create(it)
                        .withTypeText(it.signature?.type?.text)
                        .withTailText(it.module?.name?.let {"($it)"})
                        .withIcon(AllIcons.Nodes.Function)
                }
                result.addAllElements(elementBuilders)
            }
        }
    }
}