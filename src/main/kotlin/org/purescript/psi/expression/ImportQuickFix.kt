package org.purescript.psi.expression

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.purescript.psi.base.PSPsiElement
import org.purescript.psi.PSPsiFactory

class ImportQuickFix(
    private val moduleName: String,
    private val alias: String? = null,
    private val item: String? = null
) : LocalQuickFix {

    override fun getFamilyName(): String = "Import"

    override fun getName(): String = when (item) {
        null -> when (alias) {
            null -> "Import $moduleName"
            else -> "Import $moduleName as $alias"
        }

        else -> when (alias) {
            null -> "Import $moduleName ($item)"
            else -> "Import $moduleName ($item) as $alias"
        }
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val hostModule =
            (descriptor.psiElement as? PSPsiElement)?.module ?: return
        val psiFactory = project.service<PSPsiFactory>()
        val importDeclaration = psiFactory.createImportDeclaration(
            moduleName = moduleName,
            items = listOfNotNull(item),
            alias = alias
        ) ?: return
        hostModule.addImportDeclaration(importDeclaration)
    }

    companion object {
        fun allCombinations(
            moduleName: String,
            alias: String? = null,
            item: String
        ): Sequence<ImportQuickFix> = sequence {
            yield(ImportQuickFix(moduleName, alias, item))
            yield(ImportQuickFix(moduleName, alias))
        }
    }
}
