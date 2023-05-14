package org.purescript.module.declaration.value.expression.identifier

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.ProcessingContext
import org.purescript.module.declaration.type.Labeled
import org.purescript.module.declaration.type.LabeledIndex

class RowLabelCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {

        // import Module as Alias
        val project = parameters.editor.project ?: return
        val scope = GlobalSearchScope.allScope(project)
        val index = LabeledIndex
        val labels = index.getAllKeys(project)
        for (label in labels) {
            if (result.isStopped) return
            if (!result.prefixMatcher.prefixMatches(label)) continue
            val elements = index.get(label, project, scope)
            val elementBuilders = elements
                .mapNotNull { labeled: Labeled ->
                    LookupElementBuilder
                        .create(labeled, labeled.name)
                        .withTypeText(labeled.typeAsString)
                        .withTailText("(${labeled.module?.name})")
                }
            result.addAllElements(elementBuilders)
        }
    }

}