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
        val psFile = FilenameIndex.getFilesByName(
            myElement.project,
            (myElement.name ?: "").split(".").last() + ".purs",
            GlobalSearchScope.allScope(myElement.project)
        ).filterIsInstance<PSFile>()
            .firstOrNull { it.module.name == myElement.getName() ?: "" }
        return psFile?.module
    }

}
