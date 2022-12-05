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
    private val alias: String? = null,
    val item: String?
) : LocalQuickFix {

    private val importedItem: SmartPsiElementPointer<PSImportedItem>? =
        importedItem?.let { SmartPointerManager.createPointer(it) }

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
        val hostModule = (descriptor.psiElement as? PSPsiElement)?.module ?: return
        val psiFactory = PSPsiFactory(project)
        val importDeclaration = psiFactory.createImportDeclaration(
            moduleName = moduleName,
            alias = alias,
            items = listOfNotNull(item)
        ) ?: return
        hostModule.addImportDeclaration(importDeclaration)
    }
}
