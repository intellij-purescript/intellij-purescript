package org.purescript.psi.module

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.util.Computable
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.search.GlobalSearchScope
import org.purescript.psi.PSPsiFactory
import org.purescript.psi.declaration.imports.Import

class ModuleReference(element: Import.Psi) : PsiReferenceBase<Import.Psi>(
    element,
    element.moduleName.textRangeInParent,
    false
) {
    override fun getVariants(): Array<String> {
        return ModuleNameIndex().getAllKeys(element.project).toTypedArray()
    }

    override fun resolve(): Module.Psi? {
        val moduleName = element.moduleName.name
        val project = element.project
        val index = ModuleNameIndex()
        val dumbService = DumbService.getInstance(project)
        return dumbService.runReadActionInSmartMode(Computable {
            index
                .get(moduleName, project, GlobalSearchScope.allScope(project))
                .firstOrNull()
        })
    }

    override fun handleElementRename(name: String): PsiElement? {
        val oldProperName = element.moduleName
        val newProperName = PSPsiFactory(element.project).createModuleName(name)
            ?: return null
        oldProperName.replace(newProperName)
        return element
    }
}
