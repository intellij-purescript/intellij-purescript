package org.purescript.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiReferenceBase
import org.purescript.PSLanguage
import org.purescript.file.ModuleNameIndex.Companion.fileContainingModule
import org.purescript.file.ModuleNameIndex.Companion.getAllModuleNames
import org.purescript.file.PSFile
import org.purescript.psi.imports.PSImportDeclarationImpl

class ModuleReference(element: PSImportDeclarationImpl) : PsiReferenceBase<PSImportDeclarationImpl>(
    element,
    element.importName?.textRangeInParent ?: TextRange.allOf(element.text.trim()),
    false
) {
    override fun getVariants(): Array<String> {
        return getAllModuleNames(element.project).toTypedArray()
    }

    override fun resolve(): PSModule? {
        val moduleName = element.importName?.name ?: return null
        return fileContainingModule(element.project, moduleName)?.module
    }

    override fun handleElementRename(newElementName: String): PsiElement? {
        val psiFileFactory = PsiFileFactory.getInstance(element.project)
        val file = psiFileFactory.createFileFromText(
            PSLanguage.INSTANCE,
            """
                module $newElementName where
            """.trimIndent()
        ) as PSFile
        val newProperName = file.module?.nameIdentifier
            ?: return null
        element.importName?.replace(newProperName)
        return element
    }
}
