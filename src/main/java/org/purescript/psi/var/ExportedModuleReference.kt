package org.purescript.psi.`var`

import com.intellij.psi.PsiElementResolveResult.createResults
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.ResolveResult
import org.purescript.psi.PSExportedModule
import org.purescript.psi.imports.PSImportDeclarationImpl

class ExportedModuleReference(exportedModule: PSExportedModule) : PsiReferenceBase.Poly<PSExportedModule>(
    exportedModule,
    exportedModule.properName.textRangeInParent,
    false
) {

    override fun getVariants(): Array<String> {
        return candidates.mapNotNull { it.importName?.name }
            .toTypedArray()
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val importDeclarations = candidates.filter { it.importName?.name == myElement.name }
        val modules = importDeclarations.mapNotNull { it.importedModule }
        return createResults(modules)
    }

    private val candidates: Array<PSImportDeclarationImpl>
        get() =
            myElement.module.importDeclarations
}
