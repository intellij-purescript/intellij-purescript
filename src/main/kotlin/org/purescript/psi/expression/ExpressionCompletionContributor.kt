package org.purescript.psi.expression

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PlatformPatterns.psiElement

class ExpressionCompletionContributor : CompletionContributor() {
    init {
        val provider = ImportableCompletionProvider()
        extend(
            CompletionType.BASIC,
            psiElement()
                .withSuperParent(3, PSExpressionIdentifier::class.java),
            provider
        )
        extend(
            CompletionType.BASIC,
            psiElement().withSuperParent(3, PSExpressionOperator::class.java),
            provider
        )
    }

    override fun beforeCompletion(context: CompletionInitializationContext) {
        val start = context.startOffset
        val document = context.editor.document
        val char = document.getText(TextRange(start - 1, start))
        when (char) {
            "(", " ", "\n", "\t" -> { }
            "." -> {
                val range = TextRange(start - 2, start - 1)
                if (!document.getText(range).single().isLetter()) {
                    context.dummyIdentifier = "+++"
                }
            }
            else -> {
                context.dummyIdentifier = "$char$char$char"
            }
        }
    }
}