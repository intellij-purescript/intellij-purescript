package org.purescript.psi.expression

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns.psiElement
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
            val index = ExportedValueDeclNameIndex()
            val names = index.getAllKeys(project)
            val elementBuilders = names
                .map { name ->
                    LookupElementBuilder.create(name)
            }
            result.addAllElements(elementBuilders)
        }
    }
}