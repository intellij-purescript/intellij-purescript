package org.purescript.psi.`var`

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElementResolveResult.createResults
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.ResolveResult

class LocalValueReference(element: PSVar) : PsiReferenceBase.Poly<PSVar>(
    element,
    TextRange.allOf(element.text.trim()),
    false
) {

    override fun getVariants(): Array<PsiNamedElement> {
        val currentModule = myElement.module
        return currentModule.valueDeclarations.toList().toTypedArray()
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val name = myElement.text.trim()
        val module = myElement.module
        return createResults(
            module
                .valueDeclarationsByName
                .getOrDefault(name, emptyList())
                .asSequence()
                .filterNotNull()
                .toList()
        )
    }

}