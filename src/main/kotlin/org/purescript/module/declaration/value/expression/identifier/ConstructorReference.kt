package org.purescript.module.declaration.value.expression.identifier

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.LocalQuickFixProvider
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.util.parentOfType
import org.purescript.file.filesExportingConstructor
import org.purescript.ide.formatting.ImportDeclaration
import org.purescript.ide.formatting.ImportedData
import org.purescript.module.declaration.data.DataDeclaration
import org.purescript.module.declaration.imports.ImportQuickFix
import org.purescript.module.declaration.newtype.NewtypeDecl
import org.purescript.name.PSQualifiedProperName
import org.purescript.psi.PSPsiElement

class ConstructorReference(
    element: PSPsiElement,
    val qualifiedProperName: PSQualifiedProperName
) :
    LocalQuickFixProvider,
    PsiReferenceBase<PSPsiElement>(
        element,
        qualifiedProperName.properName.textRangeInParent,
        false
    ) {

    override fun getVariants(): Array<Any> =
        candidates.toList().toTypedArray()

    override fun resolve(): PsiNamedElement? =
        (candidates).firstOrNull { it.name == element.name }

    private val candidates: Sequence<PsiNamedElement>
        get() {
            val module = element.module
            val qualifyingName = qualifiedProperName.moduleName?.name
            return sequence {
                if (qualifyingName == null) yieldAll(module.constructors)
                
                val importDeclarations = module.cache.imports
                    .filter { it.importAlias?.name == qualifyingName }
                yieldAll(importDeclarations.flatMap { it.importedNewTypeConstructors })
                yieldAll(importDeclarations.flatMap { it.importedDataConstructors })
            }
        }

    override fun getQuickFixes(): Array<LocalQuickFix> {
        val qualifyingName = qualifiedProperName.moduleName?.name
        val imports = mutableListOf<ImportDeclaration>()
        for ((moduleName, typeName) in importCandidates) {
            imports += ImportDeclaration(
                moduleName,
                false,
                setOf(ImportedData(typeName, doubleDot = true)),
                qualifyingName
            )
        }
        
        return if(imports.isNotEmpty()) {
            arrayOf(ImportQuickFix(*imports.toTypedArray()))
        } else {
            arrayOf()
        }
    }

    private val importCandidates: Set<Pair<String, String>>
        get() {
            val modules = filesExportingConstructor(element.project, qualifiedProperName.name)
                .mapNotNull { it.module }

            val importCandidates = mutableSetOf<Pair<String, String>>()
            for (module in modules) {
                for (exportedConstructor in module.exportedConstructors) {
                    if (exportedConstructor.name == qualifiedProperName.name) {
                        val dataDeclaration = exportedConstructor.parentOfType<DataDeclaration>()
                        if (dataDeclaration != null) {
                            importCandidates += module.name to dataDeclaration.name
                        } else {
                            val newTypeDeclaration = exportedConstructor.parentOfType<NewtypeDecl>()
                            if (newTypeDeclaration != null) {
                                importCandidates += module.name to newTypeDeclaration.name
                            }
                        }
                    }
                }
            }
            return importCandidates
        }

}
