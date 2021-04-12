package org.purescript.psi.expression

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.LocalQuickFixProvider
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.search.FilenameIndex
import org.purescript.file.PSFile
import org.purescript.file.PSFileType

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
        return importCandidates
            .map { ImportQuickFix(it) }
            .toTypedArray()
    }

    private val importCandidates: List<String>
        get() {
            val psiManager = PsiManager.getInstance(element.project)
            val allModulesInProject = FilenameIndex
                .getAllFilesByExt(element.project, PSFileType.DEFAULT_EXTENSION)
                .mapNotNull { psiManager.findFile(it) }
                .filterIsInstance<PSFile>()
                .mapNotNull { it.module }
            return allModulesInProject
                .filter { module ->
                    module.exportedNewTypeDeclarations.any { it.newTypeConstructor.name == element.name }
                        || module.exportedDataDeclarations
                        .flatMap { it.dataConstructors.toList() }
                        .any { it.name == element.name }
                }
                .map { it.name }
        }

}
