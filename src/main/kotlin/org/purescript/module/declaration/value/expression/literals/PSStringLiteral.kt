package org.purescript.module.declaration.value.expression.literals

import com.intellij.json.psi.impl.JSStringLiteralEscaper
import com.intellij.lang.ASTNode
import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry
import org.purescript.inference.InferType
import org.purescript.module.declaration.value.Similar
import org.purescript.module.declaration.value.expression.ExpressionAtom
import org.purescript.psi.PSPsiElement

class PSStringLiteral(node: ASTNode) :
    PSPsiElement(node),
    ExpressionAtom,
    PsiLiteralValue,
    PsiLanguageInjectionHost {
    override fun areSimilarTo(other: Similar): Boolean = other is PSStringLiteral && this.text == other.text
    override fun unify() = unify(InferType.String)
    override fun getValue(): String = ElementManipulators.getValueText(this)
    override fun getReferences(): Array<PsiReference> = ReferenceProvidersRegistry.getReferencesFromProviders(this)
    override fun isValidHost(): Boolean = true
    override fun updateText(text: String): PSStringLiteral? = ElementManipulators.handleContentChange(this, text)

    override fun createLiteralTextEscaper(): LiteralTextEscaper<out PsiLanguageInjectionHost> {
        return object : JSStringLiteralEscaper<PSStringLiteral>(this) {
            override fun isRegExpLiteral(): Boolean {
                return false
            }
        }
    }
}