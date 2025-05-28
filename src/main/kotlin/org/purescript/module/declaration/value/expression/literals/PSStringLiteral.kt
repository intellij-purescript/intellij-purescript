package org.purescript.module.declaration.value.expression.literals

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
    PsiLiteralValue/*,
    TODO: PsiLanguageInjectionHost*/ {
    override fun areSimilarTo(other: Similar): Boolean = other is PSStringLiteral && this.text == other.text
    override fun unify() = unify(InferType.String)
    override fun getValue(): String = ElementManipulators.getValueText(this)
    override fun getReferences(): Array<PsiReference> = ReferenceProvidersRegistry.getReferencesFromProviders(this)
}