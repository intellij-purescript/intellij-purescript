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
        return candidates.toList()
            .distinctBy { it.name }
            .toTypedArray()
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        return createResults(*candidates.filter { it.name == myElement.name }.toTypedArray())
    }

    private val candidates: Array<PsiNamedElement>
        get() =
            myElement.module.run {
                arrayOf(
                    *valueDeclarations,
                    *foreignValueDeclarations
                )
            }
}
