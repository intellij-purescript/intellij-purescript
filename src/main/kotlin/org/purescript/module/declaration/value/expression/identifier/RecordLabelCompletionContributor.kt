package org.purescript.module.declaration.value.expression.identifier

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.patterns.PlatformPatterns.psiElement
import org.purescript.module.declaration.value.expression.literals.RecordLabel

class RecordLabelCompletionContributor : CompletionContributor() {
    init {
        val provider = RowLabelCompletionProvider()
        extend(
            CompletionType.BASIC,
            psiElement().withSuperParent(4, RecordLabel::class.java),
            provider
        )
        extend(
            CompletionType.BASIC,
            psiElement().withSuperParent(2, RecordLabel::class.java),
            provider
        )
        extend(
            CompletionType.BASIC,
            psiElement().withSuperParent(2, PSAccessor::class.java),
            provider
        )
    }
}