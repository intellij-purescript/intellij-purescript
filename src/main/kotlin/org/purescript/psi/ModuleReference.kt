package org.purescript.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import org.purescript.file.ModuleNameIndex.Companion.NAME
import org.purescript.file.ModuleNameIndex.Companion.fileContainingModule
import org.purescript.psi.imports.PSImportDeclaration
import org.purescript.psi.module.ModuleNameIndex
import org.purescript.psi.module.Module.*

class ModuleReference(element: PSImportDeclaration) : PsiReferenceBase<PSImportDeclaration>(
    element,
    element.moduleName?.textRangeInParent ?: TextRange.allOf(element.text.trim()),
    false
) {
    override fun getVariants(): Array<String> {
        return ModuleNameIndex().getAllKeys(element.project).toTypedArray()
    }

    override fun resolve(): PSModule? {
        val moduleName = element.moduleName?.name ?: return null
        val project = element.project
        return ModuleNameIndex()
            .get( moduleName, project, GlobalSearchScope.allScope(project))
            .firstOrNull()
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
