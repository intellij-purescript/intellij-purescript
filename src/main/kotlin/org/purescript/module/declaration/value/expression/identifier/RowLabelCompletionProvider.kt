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
import org.purescript.inference.Scope
import org.purescript.inference.Type
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
        val recordLiteralRowType = getRowTypeFromRecordLiteral(element)
        val recordAccessRowType = getRowTypeFromRecordAccessor(element)
        if (recordLiteralRowType != null) for ((label, type) in recordLiteralRowType.labels) {
            if (result.isStopped) return
            if (!result.prefixMatcher.prefixMatches(label)) continue
            val labelInsertHandler = SingleInsertionDeclarativeInsertHandler(":", MemberLookup)
            result.addElement(
                LookupElementBuilder
                    .create(label)
                    .withTypeText(type.toString())
                    .withTailText("(from type)")
                    .withInsertHandler(labelInsertHandler)
            )
        }
        if (recordAccessRowType != null) for ((label, type) in recordAccessRowType.labels) {
            if (result.isStopped) return
            if (!result.prefixMatcher.prefixMatches(label)) continue
            result.addElement(
                LookupElementBuilder
                    .create(label)
                    .withTypeText(type.toString())
                    .withTailText("(from type)")
            )
        }
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

    private fun getRowType(element: PsiElement): Type.Row? =
        getRowTypeFromRecordLiteral(element) 
            ?: getRowTypeFromRecordAccessor(element)

    private fun getRowTypeFromRecordAccessor(element: PsiElement): Type.Row? =
        (element.parentOfType<RecordAccess>()?.record?.infer(Scope.new()) as? Type.App)?.on as? Type.Row

    private fun getRowTypeFromRecordLiteral(element: PsiElement): Type.Row? {
        val recordLiteral = element.parentOfType<RecordLiteral>()
        val function = ((recordLiteral?.parent as? Argument)?.parent as? Call)?.function
        return (((function?.infer(Scope.new()) as? Type.App)?.f as? Type.App)?.on as? Type.App)?.on as? Type.Row
    }

}