package org.purescript.psi.expression

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import org.purescript.file.PSFile
import org.purescript.file.PSFileType
import org.purescript.psi.PSModule
import org.purescript.psi.PSPsiFactory

class ImportExpressionConstructorQuickFix(
    val nameToImport: String,
    val hostModule: PSModule,
    val importedModule: PSModule?
) : LocalQuickFix {

    override fun getFamilyName(): String = "Import"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val importedModule = importedModule
            ?: return
        val psiFactory = PSPsiFactory(project)
        val importDeclaration =
            psiFactory.createImportDeclaration(importedModule.name)
                ?: return
        val lastImportDeclaration =
            this.hostModule.importDeclarations.lastOrNull()
        val insertPosition =
            lastImportDeclaration ?: this.hostModule.whereKeyword
        val newLine = psiFactory.createNewLine()
        this.hostModule.addAfter(importDeclaration, insertPosition)
        this.hostModule.addAfter(newLine, insertPosition)
        if (lastImportDeclaration == null) {
            this.hostModule.addAfter(newLine, insertPosition)
        }
    }

    companion object {
        fun getCandidateModules(project: Project, expressionConstructorName: String): List<PSModule> {
            val psiManager = PsiManager.getInstance(project)
            return FilenameIndex.getAllFilesByExt(project, PSFileType.DEFAULT_EXTENSION)
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
