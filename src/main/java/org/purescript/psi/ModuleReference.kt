package org.purescript.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import org.purescript.file.PSFile
import org.purescript.psi.imports.PSImportDeclarationImpl

class ModuleReference(element: PSImportDeclarationImpl) : PsiReferenceBase<PSImportDeclarationImpl>(
    element,
    element.importName?.textRangeInParent ?: TextRange.allOf(element.text.trim()) ,
    false
) {
    override fun resolve(): PSModule? {
        val moduleName = element.importName?.name
            ?: return null
        val fileName = moduleName.split(".").last() + ".purs"
        return FilenameIndex.getFilesByName(
            myElement.project,
            fileName,
            GlobalSearchScope.allScope(myElement.project)
        ).filterIsInstance<PSFile>()
            .mapNotNull { it.module }
            .firstOrNull { it.name == moduleName }
    }

}
