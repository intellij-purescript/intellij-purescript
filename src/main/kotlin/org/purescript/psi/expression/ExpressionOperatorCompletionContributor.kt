package org.purescript.psi.expression

import com.intellij.codeInsight.completion.*
import com.intellij.patterns.PlatformPatterns.psiElement

class ExpressionOperatorCompletionContributor : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC,
            psiElement().withSuperParent(3, PSExpressionOperator::class.java),
            ImportableCompletionProvider()
        )
    }

    override fun beforeCompletion(context: CompletionInitializationContext) {
    }
}