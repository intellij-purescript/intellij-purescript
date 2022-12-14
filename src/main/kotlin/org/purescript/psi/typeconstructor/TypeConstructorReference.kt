package org.purescript.psi.typeconstructor

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.LocalQuickFixProvider
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiReferenceBase
import org.purescript.file.ExportedTypesIndex
import org.purescript.psi.expression.ImportQuickFix

class TypeConstructorReference(typeConstructor: PSTypeConstructor) :
    LocalQuickFixProvider,
    PsiReferenceBase<PSTypeConstructor>(
        typeConstructor,
        typeConstructor.textRangeInParent,
        false
    ) {

    override fun getVariants(): Array<PsiNamedElement> =
        candidates.toTypedArray()

    override fun resolve(): PsiNamedElement? =
        candidates.firstOrNull { it.name == myElement.name }

    /**
     * Type constructors can reference any data, new type, or synonym declaration
     * in the current module or any of the imported modules.
     */
    private val candidates: List<PsiNamedElement>
        get() {
            val name = element.moduleName?.name
            return if (name != null) {
                candidatesFor(name)
            } else {
                allCandidates
            }
        }

    private fun candidatesFor(name: String): List<PsiNamedElement> {
        val module = element.module ?: return emptyList()
        val importDeclaration = module
            .importDeclarations
            .firstOrNull { it.importAlias?.name == name }
            ?: return emptyList()
        val candidates = mutableListOf<PsiNamedElement>()
        candidates.addAll(importDeclaration.importedDataDeclarations)
        candidates.addAll(importDeclaration.importedNewTypeDeclarations)
        candidates.addAll(importDeclaration.importedTypeSynonymDeclarations)
        candidates.addAll(importDeclaration.importedForeignDataDeclarations)
        return candidates
    }

    private val allCandidates: List<PsiNamedElement> get() {
        val module = element.module ?: return emptyList()
        val candidates = mutableListOf<PsiNamedElement>()
        candidates.addAll(module.dataDeclarations)
        candidates.addAll(module.newTypeDeclarations)
        candidates.addAll(module.typeSynonymDeclarations)
        candidates.addAll(module.foreignDataDeclarations)
        for (importDeclaration in module.importDeclarations) {
            candidates.addAll(importDeclaration.importedDataDeclarations)
            candidates.addAll(importDeclaration.importedNewTypeDeclarations)
            candidates.addAll(importDeclaration.importedTypeSynonymDeclarations)
            candidates.addAll(importDeclaration.importedForeignDataDeclarations)
        }
        return candidates
    }

    override fun getQuickFixes(): Array<LocalQuickFix> {
        return importCandidates
            .map { ImportQuickFix(it, item = element.name) }
            .toTypedArray()
    }

    private val importCandidates: List<String>
        get() = ExportedTypesIndex
            .filesExportingType(element.project, element.name)
            .mapNotNull { it.module?.name }
}
