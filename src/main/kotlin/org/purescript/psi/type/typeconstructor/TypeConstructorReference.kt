package org.purescript.psi.type.typeconstructor

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.LocalQuickFixProvider
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.search.GlobalSearchScope
import org.purescript.psi.declaration.ImportableTypeIndex
import org.purescript.psi.declaration.imports.ImportQuickFix
import org.purescript.psi.module.Module

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
        val module: Module = element.module ?: return emptyList()
        val importDeclaration = module
            .cache.imports
            .firstOrNull { it.importAlias?.name == name }
            ?: return emptyList()
        val candidates = mutableListOf<PsiNamedElement>()
        candidates.addAll(importDeclaration.importedDataDeclarations)
        candidates.addAll(importDeclaration.importedNewTypeDeclarations)
        candidates.addAll(importDeclaration.importedTypeSynonymDeclarations)
        candidates.addAll(importDeclaration.importedForeignDataDeclarations)
        candidates.addAll(importDeclaration.importedClassDeclarations)
        return candidates
    }

    private val allCandidates: List<PsiNamedElement>
        get() {
            val module: Module = element.module ?: return emptyList()
            val candidates = mutableListOf<PsiNamedElement>()
            candidates.addAll(module.cache.dataDeclarations)
            candidates.addAll(module.cache.newTypeDeclarations)
            candidates.addAll(module.cache.typeSynonymDeclarations)
            candidates.addAll(module.cache.foreignDataDeclarations)
            candidates.addAll(module.cache.classDeclarations)
            for (importDeclaration in module.cache.imports) {
                candidates.addAll(importDeclaration.importedDataDeclarations)
                candidates.addAll(importDeclaration.importedNewTypeDeclarations)
                candidates.addAll(importDeclaration.importedTypeSynonymDeclarations)
                candidates.addAll(importDeclaration.importedForeignDataDeclarations)
                candidates.addAll(importDeclaration.importedClassDeclarations)
            }
            return candidates
        }

    override fun getQuickFixes(): Array<LocalQuickFix> {
        val scope = GlobalSearchScope.allScope(element.project)
        val imports = ImportableTypeIndex
            .get(element.name, element.project, scope)
            .mapNotNull { it.asImport()?.withAlias(element.qualifierName) }.toTypedArray()
        return if(imports.isNotEmpty()) {
            arrayOf(ImportQuickFix(*imports))
        } else {
            arrayOf()
        }
    }

}
