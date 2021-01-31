package org.purescript.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import org.purescript.file.PSFile

class ModuleReference(element: PSImportDeclarationImpl) : PsiReferenceBase<PSImportDeclarationImpl>(
    element,
    TextRange.allOf(element.text.trim()),
    false
) {
    override fun resolve(): PSModule? {
        val psFile = FilenameIndex.getFilesByName(
            myElement.project,
            myElement.importName + ".purs",
            GlobalSearchScope.allScope(myElement.project)
        ).first() as PSFile?
        return psFile?.module
    }

}
