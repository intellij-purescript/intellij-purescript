package org.purescript.psi.expression

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer
import org.purescript.psi.PSPsiElement
import org.purescript.psi.PSPsiFactory
import org.purescript.psi.imports.PSImportedItem

class ImportQuickFix(
    private val moduleName: String,
    importedItem: PSImportedItem? = null,
    private val alias: String? = null
) : LocalQuickFix {

    private val importedItem: SmartPsiElementPointer<PSImportedItem>? =
        importedItem?.let { SmartPointerManager.createPointer(it) }

    override fun getFamilyName(): String = "Import"

    override fun getName(): String =
        importedItem?.element?.let { "Import $moduleName (${it.text})" }
            ?: "Import $moduleName"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val hostModule = (descriptor.psiElement as? PSPsiElement)?.module ?: return
        val psiFactory = PSPsiFactory(project)
        val importedItems = importedItem?.element?.let { listOf(it) } ?: emptyList()
        val importDeclaration = psiFactory.createImportDeclaration(
            moduleName = moduleName,
            importedItems = importedItems,
            alias = alias
        ) ?: return
        hostModule.addImportDeclaration(importDeclaration)
    }
}
