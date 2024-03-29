package org.purescript.module

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.search.GlobalSearchScope
import org.purescript.psi.PSPsiFactory
import org.purescript.module.declaration.imports.Import

class ModuleReference(element: Import) : PsiReferenceBase<Import>(
    element,
    element.moduleName.textRangeInParent,
    false
) {
    override fun getVariants(): Array<String> {
        return ModuleNameIndex().getAllKeys(element.project).toTypedArray()
    }

    override fun resolve(): Module? {
        val moduleName = element.moduleName.name
        val project = element.project
        val index = ModuleNameIndex()
        return index.get(moduleName, project, GlobalSearchScope.allScope(project)).firstOrNull { it.isValid }
    }

    override fun handleElementRename(name: String): PsiElement? {
        val oldProperName = element.moduleName
        val newProperName = PSPsiFactory(element.project).createModuleName(name)
            ?: return null
        oldProperName.replace(newProperName)
        return element
    }
}
