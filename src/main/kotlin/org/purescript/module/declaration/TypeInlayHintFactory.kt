package org.purescript.module.declaration

import com.intellij.codeInsight.hints.declarative.InlayHintsProviderFactory
import com.intellij.codeInsight.hints.declarative.InlayProviderInfo
import com.intellij.lang.Language
import org.purescript.PSLanguage

class TypeInlayHintFactory : InlayHintsProviderFactory {
    override fun getSupportedLanguages(): Set<Language> = setOf(PSLanguage)
    override fun getProviderInfo(l: Language, id: String) = providers.firstOrNull { it.providerId == id }
    override fun getProvidersForLanguage(language: Language) = providers
    private val providers = listOf(
        InlayProviderInfo(
            SignatureInlayHintProvider(),
            "purescript.signature",
            setOf(),
            true,
            "Signature"
        )
    )
}