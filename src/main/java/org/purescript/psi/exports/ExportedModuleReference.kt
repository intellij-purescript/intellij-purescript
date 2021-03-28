package org.purescript.psi.exports

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import org.purescript.psi.imports.PSImportDeclarationImpl

class ExportedModuleReference(exportedModule: PSExportedModule) : PsiReferenceBase<PSExportedModule>(
    exportedModule,
    exportedModule.properName.textRangeInParent,
    false
) {
    override fun getVariants(): Array<String> {
        return candidates.mapNotNull { it.name }
            .toTypedArray()
    }

    override fun resolve(): PsiElement? =
        candidates.firstOrNull { it.name == myElement.name }
            ?.run { importAlias ?: importedModule }

    private val candidates: Array<PSImportDeclarationImpl>
        get() =
            myElement.module?.importDeclarations ?: emptyArray()
}
