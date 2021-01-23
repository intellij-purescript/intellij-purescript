package org.purescript.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.PsiElementResolveResult.createResults
import org.jetbrains.annotations.NotNull
import org.purescript.file.PSFile

class ValueReference(element: @NotNull PSPsiElement) : PsiReferenceBase.Poly<PSPsiElement?>(
    element,
    TextRange.allOf(element.text.trim()),
    false
) {

    override fun getVariants(): Array<PsiNamedElement> {
        return (myElement?.containingFile as PSFile)
            .topLevelValueDeclarations
            .values
            .flatten()
            .toTypedArray()
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val name = myElement?.text?.trim()
        val file = myElement?.containingFile as PSFile?
        val declarations =
            file?.topLevelValueDeclarations?.get(name) ?: listOf()
        return createResults(declarations)
    }

}