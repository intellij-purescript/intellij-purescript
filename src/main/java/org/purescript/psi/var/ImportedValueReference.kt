package org.purescript.psi.`var`

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.LocalQuickFixProvider
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElementResolveResult.createResults
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.ResolveResult
import org.purescript.file.ExportedValuesIndex
import org.purescript.psi.expression.ImportQuickFix

class ImportedValueReference(element: PSVar) :
    LocalQuickFixProvider,
    PsiReferenceBase.Poly<PSVar>(
        element,
        TextRange.allOf(element.text.trim()),
        false
    ) {

    override fun getVariants(): Array<PsiNamedElement> {
        val currentModule = myElement.module
            ?: return emptyArray()
        return currentModule.importDeclarations
            .flatMap { it.importedValueDeclarations }
            .toTypedArray()
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        return createResults(
            variants
                .filter { it.name == myElement.name }
                .toList()
        )
    }

    override fun getQuickFixes(): Array<LocalQuickFix> =
        ExportedValuesIndex
            .filesExportingValue(element.project, element.name)
            .mapNotNull { it.module?.name }
            .map { ImportQuickFix(it) }
            .toTypedArray()


}
