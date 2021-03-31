package org.purescript.psi.exports

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiReferenceBase
import org.purescript.PSLanguage
import org.purescript.file.PSFile
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
        element.properName.replace(newProperName)
        return element
    }

    private val candidates: Array<PSImportDeclarationImpl>
        get() =
            myElement.module?.importDeclarations ?: emptyArray()
}
