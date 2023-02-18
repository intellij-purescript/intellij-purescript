package org.purescript.psi.type

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PlatformPatterns.psiElement
import org.purescript.psi.type.typeconstructor.PSTypeConstructor

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