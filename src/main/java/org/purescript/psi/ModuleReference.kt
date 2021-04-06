package org.purescript.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import org.purescript.file.ModuleNameIndex.Companion.fileContainingModule
import org.purescript.file.ModuleNameIndex.Companion.getAllModuleNames
import org.purescript.psi.imports.PSImportDeclarationImpl

class ModuleReference(element: PSImportDeclarationImpl) : PsiReferenceBase<PSImportDeclarationImpl>(
    element,
    element.moduleName?.textRangeInParent ?: TextRange.allOf(element.text.trim()),
    false
) {
    override fun getVariants(): Array<String> {
        return getAllModuleNames(element.project).toTypedArray()
    }

    override fun resolve(): PSModule? {
        val moduleName = element.moduleName?.name ?: return null
        return fileContainingModule(element.project, moduleName)?.module
    }

    override fun handleElementRename(name: String): PsiElement? {
        val oldProperName = element.moduleName
            ?: return null
        val newProperName = PSPsiFactory(element.project).createModuleName(name)
            ?: return null
        oldProperName.replace(newProperName)
        return element
    }
}
