package org.purescript.psi.expression

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.purescript.ide.formatting.ImportDeclaration
import org.purescript.ide.formatting.ImportedItem
import org.purescript.psi.PSPsiFactory
import org.purescript.psi.base.PSPsiElement
import java.util.*

class ImportQuickFix(val import: ImportDeclaration) : LocalQuickFix {

    override fun getFamilyName(): String = "Import"

    override fun getName(): String = import.toString().replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val hostModule = (descriptor.psiElement as? PSPsiElement)?.module
            ?: return
        val psiFactory = project.service<PSPsiFactory>()
        val importDeclaration = psiFactory.createImportDeclaration(import)
        hostModule.addImportDeclaration(importDeclaration)
    }

    companion object {
        fun allCombinations(
            moduleName: String,
            alias: String? = null,
            item: ImportedItem
        ): Sequence<ImportQuickFix> = sequence {
            val importWithItem = ImportDeclaration(
                moduleName,
                alias = alias,
                importedItems = setOf(item)
            )
            yield(ImportQuickFix(importWithItem))
            yield(ImportQuickFix(ImportDeclaration(moduleName, alias = alias)))
        }
    }
}
