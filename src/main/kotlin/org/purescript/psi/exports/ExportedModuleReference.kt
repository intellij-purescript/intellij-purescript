package org.purescript.psi.exports

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import org.purescript.psi.PSPsiFactory
import org.purescript.psi.imports.PSImportDeclarationImpl

class ExportedModuleReference(exportedModule: PSExportedModule) : PsiReferenceBase<PSExportedModule>(
    exportedModule,
    exportedModule.moduleName.textRangeInParent,
    false
) {
    override fun getVariants(): Array<String> {
        return candidates.mapNotNull { it.name }
            .toTypedArray()
    }

    override fun resolve(): PsiElement? {
        if (element.name == myElement.module?.name) {
            return myElement.module
        } else {
            return candidates.firstOrNull { it.name == myElement.name }
                ?.run { importAlias ?: importedModule }
        }
    }

    override fun handleElementRename(name: String): PsiElement? {
        val newProperName = PSPsiFactory(element.project).createModuleName(name)
            ?: return null
        element.moduleName.replace(newProperName)
        return element
    }

    private val candidates: Array<PSImportDeclarationImpl>
        get() =
            myElement.module?.importDeclarations ?: emptyArray()
}
