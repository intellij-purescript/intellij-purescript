package org.purescript.keyword

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.patterns.PlatformPatterns
import org.purescript.parser.ExpressionIdentifier

class KeywordCompletionContributor : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement()
                .withAncestor(4, PlatformPatterns.psiElement(ExpressionIdentifier)),
            KeywordCompletionProvider()
        )
    }
}