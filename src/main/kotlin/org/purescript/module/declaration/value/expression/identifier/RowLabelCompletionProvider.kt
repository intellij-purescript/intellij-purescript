package org.purescript.module.declaration.value.expression.identifier

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.DeclarativeInsertHandler2.PopupOptions.MemberLookup
import com.intellij.codeInsight.completion.SingleInsertionDeclarativeInsertHandler
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.parentOfType
import com.intellij.util.ProcessingContext
import org.purescript.inference.InferType
import org.purescript.inference.inferType
import org.purescript.module.declaration.type.Labeled
import org.purescript.module.declaration.type.LabeledIndex
import org.purescript.module.declaration.value.expression.RecordAccess
import org.purescript.module.declaration.value.expression.literals.RecordLiteral

class RowLabelCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {

        // import Module as Alias
        val project = parameters.editor.project ?: return
        val element = parameters.position
        val functionCallLabels = getRowTypeFromFunctionCall(element)?.mergedLabels()
        val literalLabels = getRowTypeFromLiteral(element)?.mergedLabels()
        val recordAccessRowType = getRowTypeFromRecordAccessor(element)
        if (functionCallLabels != null) for ((label, type) in functionCallLabels) {
            if (result.isStopped) return
            if (!result.prefixMatcher.prefixMatches(label)) continue
            if (literalLabels?.contains(label to type) == true) continue
            val labelInsertHandler = SingleInsertionDeclarativeInsertHandler(":", MemberLookup)
            result.addElement(
                LookupElementBuilder
                    .create(label)
                    .withTypeText(type.toString())
                    .withTailText("(from type)")
                    .withInsertHandler(labelInsertHandler)
            )
        }
        if (recordAccessRowType != null) for ((label, type) in recordAccessRowType.mergedLabels()) {
            if (result.isStopped) return
            if (!result.prefixMatcher.prefixMatches(label)) continue
            result.addElement(
                LookupElementBuilder
                    .create(label)
                    .withTypeText(type.toString())
                    .withTailText("(from type)")
            )
        }
        if (parameters.isExtendedCompletion) {
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

    private fun getRowTypeFromRecordAccessor(element: PsiElement): InferType.Row? {
        val type = element.parentOfType<RecordAccess>()?.record?.inferType()
        return (type as? InferType.App)?.on as? InferType.RowList
    }

    private fun getRowTypeFromFunctionCall(element: PsiElement): InferType.Row? {
        val recordLiteral = element.parentOfType<RecordLiteral>()
        val function = ((recordLiteral?.parent as? Argument)?.parent as? Call)?.function
        return ((function?.inferType()?.argument) as? InferType.App)?.on as? InferType.Row
    }

    private fun getRowTypeFromLiteral(element: PsiElement): InferType.Row? {
        val recordLiteral = element.parentOfType<RecordLiteral>()?.inferType()
        return (recordLiteral as? InferType.App)?.on as? InferType.Row
    }

}