package org.purescript.module.declaration

import com.intellij.codeInsight.hints.declarative.InlayHintsProviderFactory
import com.intellij.codeInsight.hints.declarative.InlayProviderInfo
import com.intellij.lang.Language
import org.purescript.PSLanguage

class SignatureInlayHintFactory: InlayHintsProviderFactory {
    override fun getSupportedLanguages(): Set<Language> = setOf(PSLanguage)
    private val inlayProviderInfo =
        InlayProviderInfo(
            SignatureInlayHintProvider(),
            "purescript.signature",
            setOf(),
            true,
            "Signature"
        )

    override fun getProviderInfo(language: Language, providerId: String): InlayProviderInfo? = 
        inlayProviderInfo

    override fun getProvidersForLanguage(language: Language): List<InlayProviderInfo> = 
        listOf(inlayProviderInfo)
}