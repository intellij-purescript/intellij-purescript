package org.purescript.psi.expression

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import org.purescript.file.PSFile
import org.purescript.file.PSFileType
import org.purescript.psi.PSModule
import org.purescript.psi.PSPsiElement
import org.purescript.psi.PSPsiFactory

class ImportQuickFix(private val moduleNameToImport: String) : LocalQuickFix {

    override fun getFamilyName(): String = "Import"
    override fun getName() = "Import ${moduleNameToImport}"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val hostModule = (descriptor.psiElement as? PSPsiElement)?.module ?: return
        val psiFactory = PSPsiFactory(project)
        val importDeclaration =
            psiFactory.createImportDeclaration(moduleNameToImport)
                ?: return

        val lastImportDeclaration = hostModule.importDeclarations.lastOrNull()
        val insertPosition = lastImportDeclaration ?: hostModule.whereKeyword
        val newLine = psiFactory.createNewLine()
        hostModule.addAfter(importDeclaration, insertPosition)
        hostModule.addAfter(newLine, insertPosition)
        if (lastImportDeclaration == null) {
            hostModule.addAfter(newLine, insertPosition)
        }

    }

    companion object {
        fun getCandidateModules(
            project: Project,
            expressionConstructorName: String
        ): List<PSModule> {
            val psiManager = PsiManager.getInstance(project)
            return FilenameIndex
                .getAllFilesByExt(project, PSFileType.DEFAULT_EXTENSION)
                .mapNotNull { psiManager.findFile(it) }
                .filterIsInstance<PSFile>()
                .mapNotNull { it.module }
                .filter { module ->
                    module.exportedNewTypeDeclarations.any { it.newTypeConstructor.name == expressionConstructorName }
                        || module.exportedDataDeclarations
                        .flatMap { it.dataConstructors.toList() }
                        .any { it.name == expressionConstructorName }
                }
        }
    }
}
