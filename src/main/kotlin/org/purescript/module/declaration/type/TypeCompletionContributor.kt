package org.purescript.module.declaration.type

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.patterns.PlatformPatterns.psiElement
import org.purescript.module.declaration.type.typeconstructor.PSTypeConstructor

class TypeCompletionContributor : CompletionContributor() {
    init {
        val provider = ImportableTypeCompletionProvider()
        extend(
            CompletionType.BASIC,
            psiElement().withSuperParent(3, PSTypeConstructor::class.java),
            provider
        )
    }

}