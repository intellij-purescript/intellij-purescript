package org.purescript.module.declaration.value.expression.identifier

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionInitializationContext
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PlatformPatterns.psiElement
import org.purescript.module.declaration.value.binder.ConstructorBinder

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
        extend(
            CompletionType.BASIC,
            psiElement().withSuperParent(3, PSExpressionConstructor::class.java),
            provider
        )
        extend(
            CompletionType.BASIC,
            psiElement().withSuperParent(3, ConstructorBinder::class.java),
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
                if (document.getText(range).single().isLetter()) {
                    context.dummyIdentifier = "foo"
                } else {
                    context.dummyIdentifier = "+++"
                }
            }
            else -> {
                context.dummyIdentifier = "$char$char$char"
            }
        }
    }
}