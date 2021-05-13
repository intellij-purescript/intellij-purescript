package org.purescript.psi.expression

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import org.purescript.psi.PSPsiElement
import org.purescript.psi.PSPsiFactory

class ImportQuickFix(private val moduleNameToImport: String) : LocalQuickFix {

    override fun getFamilyName(): String = "Import"

    override fun getName() = "Import $moduleNameToImport"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val hostModule = (descriptor.psiElement as? PSPsiElement)?.module ?: return
        val psiFactory = PSPsiFactory(project)
        val importDeclaration = psiFactory.createImportDeclaration(moduleNameToImport)
            ?: return
        hostModule.addImportDeclaration(importDeclaration)
    }
}
