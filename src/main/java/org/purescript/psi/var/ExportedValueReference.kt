package org.purescript.psi.`var`

import com.intellij.psi.PsiElementResolveResult.createResults
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.ResolveResult
import org.purescript.psi.PSExportedValue

class ExportedValueReference(exportedValue: PSExportedValue) : PsiReferenceBase.Poly<PSExportedValue>(
    exportedValue,
    exportedValue.identifier.textRangeInParent,
    false
) {

    override fun getVariants(): Array<PsiNamedElement> {
        return myElement.module.valueDeclarations.toList().toTypedArray()
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        return myElement.module.valueDeclarations.filter { it.name == myElement.name }
            .let { createResults(*it.toList().toTypedArray()) }
    }
}
