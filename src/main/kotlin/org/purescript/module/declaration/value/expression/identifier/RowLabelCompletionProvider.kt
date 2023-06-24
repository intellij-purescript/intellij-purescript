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
import org.purescript.inference.InferType
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
        val functionCallLabels = getRowTypeFromFunctionCall(element)?.labels
        val literalLabels = getRowTypeFromLiteral(element)?.labels
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

    private fun getRowTypeFromRecordAccessor(element: PsiElement): InferType.RowList? =
        (element.parentOfType<RecordAccess>()?.record?.infer(Scope.new()) as? InferType.App)?.on as? InferType.RowList

    private fun getRowTypeFromFunctionCall(element: PsiElement): InferType.RowList? {
        val recordLiteral = element.parentOfType<RecordLiteral>()
        val function = ((recordLiteral?.parent as? Argument)?.parent as? Call)?.function
        val scope = Scope.new()
        val functionType = function?.infer(scope)
        return when (functionType) {
            is InferType.Constraint -> {
                val (constraint, expr) = functionType
                val cleanExpr = (constraint as? InferType.App)
                    ?.let { (f, union) ->
                        (f as? InferType.App)?.let { (f, right) ->
                            (f as? InferType.App)?.let { (f, left) ->
                                if (
                                    f == InferType.Constructor("Union") &&
                                    right is InferType.RowList &&
                                    left is InferType.RowList
                                ) {
                                    val merge = InferType.RowList(left.labels + right.labels)
                                    scope.unify(merge, union)
                                    scope.substitute(expr)
                                } else expr
                            }
                        }
                    } as? InferType.App
                ((cleanExpr?.f
                        as? InferType.App)?.on
                        as? InferType.App)?.on
                        as? InferType.RowList
            }

            is InferType.App -> ((functionType.f as? InferType.App)?.on as? InferType.App)?.on as? InferType.RowList
            else -> null
        }

    }

    private fun getRowTypeFromLiteral(element: PsiElement): InferType.RowList? {
        val recordLiteral = element.parentOfType<RecordLiteral>()?.infer(Scope.new())
        return (recordLiteral as? InferType.App)?.on as? InferType.RowList
    }

}