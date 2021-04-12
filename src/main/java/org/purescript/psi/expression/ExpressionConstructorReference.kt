package org.purescript.psi.expression

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.LocalQuickFixProvider
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.search.FilenameIndex
import org.purescript.file.PSFile
import org.purescript.file.PSFileType
import org.purescript.psi.PSModule

class ExpressionConstructorReference(expressionConstructor: PSExpressionConstructor) :
    LocalQuickFixProvider,
    PsiReferenceBase<PSExpressionConstructor>(
        expressionConstructor,
        expressionConstructor.qualifiedProperName.properName.textRangeInParent,
        false
    ) {

    override fun getVariants(): Array<Any> =
        candidates.toList().toTypedArray()

    override fun resolve(): PsiNamedElement? =
        candidates.firstOrNull { it.name == element.name }

    private val candidates: Sequence<PsiNamedElement>
        get() {
            val module = element.module ?: return emptySequence()
            val qualifyingName = element.qualifiedProperName.moduleName?.name
            if (qualifyingName != null) {
                val importDeclaration = module.importDeclarations
                    .firstOrNull { it.importAlias?.name == qualifyingName }
                    ?: return emptySequence()
                return sequence {
                    yieldAll(importDeclaration.importedNewTypeConstructors)
                    yieldAll(importDeclaration.importedDataConstructors)
                }
            } else {
                return sequence {
                    yieldAll(module.newTypeConstructors)
                    yieldAll(module.dataConstructors)
                    val importDeclarations = module.importDeclarations
                        .filter { it.importAlias == null }
                    yieldAll(importDeclarations.flatMap { it.importedNewTypeConstructors })
                    yieldAll(importDeclarations.flatMap { it.importedDataConstructors })
                }
            }
        }

    override fun getQuickFixes(): Array<LocalQuickFix> {
        val candidateModules =
            getCandidateModules(element.project, element.name)
        return candidateModules
            .map { ImportQuickFix(it.name) }
            .toTypedArray()
    }

    private fun getCandidateModules(
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
