package org.purescript.psi.`var`

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElementResolveResult.createResults
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.ResolveResult

class ImportedValueReference(element: PSVar) : PsiReferenceBase.Poly<PSVar>(
    element,
    TextRange.allOf(element.text.trim()),
    false
) {

    override fun getVariants(): Array<PsiNamedElement> {
        val currentModule = myElement.module
        return currentModule.importDeclarations
            .flatMap { it.importedValues }
            .toTypedArray()
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        return createResults(
            variants
                .filter { it.name == myElement.name }
                .toList()
        )
    }

}