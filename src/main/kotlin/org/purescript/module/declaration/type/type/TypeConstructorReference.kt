package org.purescript.module.declaration.type.type

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.LocalQuickFixProvider
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import org.purescript.PSLanguage
import org.purescript.module.Module
import org.purescript.module.declaration.Importable
import org.purescript.module.declaration.ImportableTypeIndex
import org.purescript.module.declaration.foreign.PSForeignDataDeclaration
import org.purescript.module.declaration.imports.ImportQuickFix

class TypeConstructorReference(typeConstructor: PSTypeConstructor) :
    LocalQuickFixProvider,
    PsiReferenceBase<PSTypeConstructor>(typeConstructor, typeConstructor.identifier.textRangeInParent, false) {

    override fun getVariants(): Array<PsiNamedElement> = candidates.toTypedArray()
    override fun resolve(): PsiNamedElement? = 
        candidates.firstOrNull { it.name == element.name } 
        ?: fromPrim()

    private fun fromPrim(): PSForeignDataDeclaration? {
        val primModule = PSLanguage
            .getPrimModule(element.project)
        return primModule
            ?.cache?.foreignDataDeclarations
            ?.firstOrNull { it.name == element.name }
    }

    /**
     * Type constructors can reference any data, new type, or synonym declaration
     * in the current module or any of the imported modules.
     */
    private val candidates: List<PsiNamedElement>
        get() {
            val qualifier = element.moduleName?.name
            return if (qualifier != null) {
                candidatesFor(qualifier)
            } else {
                allCandidatesWithoutAlias
            }
        }

    private fun candidatesFor(qualifier: String): List<PsiNamedElement> {
        val module: Module = element.module
        val importDeclaration = module.cache.imports.filter { it.importAlias?.name == qualifier }
        return importDeclaration.flatMap { it.importedTypeNames }.toMutableList()
    }

    private val allCandidatesWithoutAlias: List<PsiNamedElement>
        get() {
            val module: Module = element.module
            val candidates = mutableListOf<PsiNamedElement>()
            candidates.addAll(module.cache.dataDeclarations)
            candidates.addAll(module.cache.newTypeDeclarations)
            candidates.addAll(module.cache.typeSynonymDeclarations)
            candidates.addAll(module.cache.foreignDataDeclarations)
            candidates.addAll(module.cache.classDeclarations)
            for (importDeclaration in module.cache.imports) {
                if (importDeclaration.importAlias != null) continue
                candidates.addAll(importDeclaration.importedTypeNames)
            }
            return candidates
        }

    override fun getQuickFixes(): Array<LocalQuickFix> {
        val scope = GlobalSearchScope.allScope(element.project)
        val imports = StubIndex.getElements(
            ImportableTypeIndex.key,
            element.name,
            element.project,
            scope,
            Importable::class.java
        ).mapNotNull { it.asImport()?.withAlias(element.qualifierName) }.toTypedArray()
        return if (imports.isNotEmpty()) {
            arrayOf(ImportQuickFix(*imports))
        } else {
            arrayOf()
        }
    }

}
