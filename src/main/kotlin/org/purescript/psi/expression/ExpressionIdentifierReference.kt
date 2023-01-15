package org.purescript.psi.expression

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.LocalQuickFixProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.util.parents
import org.purescript.file.ExportedValuesIndex
import org.purescript.ide.formatting.ImportedValue
import org.purescript.psi.PSPsiFactory
import org.purescript.psi.declaration.value.ValueDecl
import org.purescript.psi.expression.dostmt.PSDoBlock

class ExpressionIdentifierReference(expressionConstructor: PSExpressionIdentifier) :
    LocalQuickFixProvider,
    PsiReferenceBase<PSExpressionIdentifier>(
        expressionConstructor,
        expressionConstructor.qualifiedIdentifier.identifier.textRangeInParent,
        false
    ) {

    override fun getVariants(): Array<Any> =
        candidates.toList().toTypedArray()

    override fun resolve(): PsiNamedElement? {
        val name = element.name
        return candidates.firstOrNull { it.name == name }
    }

    private val candidates: Sequence<PsiNamedElement>
        get() {
            val module = element.module ?: return emptySequence()
            val qualifyingName = element.qualifiedIdentifier.moduleName?.name
            return sequence {
                if (qualifyingName == null) {
                    for (parent in element.parents(false)) {
                        when (parent) {
                            is ValueDecl.Psi -> {
                                yieldAll(parent.varBindersInParameters.values)
                                val valueDeclarations = parent
                                    .where
                                    ?.valueDeclarations
                                    ?.asSequence()
                                    ?: sequenceOf()
                                yieldAll(valueDeclarations)
                            }

                            is PSExpressionWhere -> {
                                val valueDeclarations = parent
                                    .where
                                    ?.valueDeclarations
                                    ?.asSequence()
                                    ?: sequenceOf()
                                yieldAll(valueDeclarations)
                            }

                            is PSLet ->
                                yieldAll(parent.valueDeclarations.asSequence())

                            is PSDoBlock ->
                                yieldAll(parent.valueDeclarations)
                        }
                    }
                    // TODO Support values defined in the expression
                    yieldAll(module.cache.valueDeclarations.toList())
                    yieldAll(module.cache.foreignValueDeclarations.toList())
                    val localClassMembers = module
                        .cache.classes
                        .asSequence()
                        .flatMap { it.classMembers.asSequence() }
                    yieldAll(localClassMembers)
                }
                val importDeclarations =
                    module.cache.imports.filter { it.importAlias?.name == qualifyingName }
                yieldAll(importDeclarations.flatMap { it.importedValueDeclarations })
                yieldAll(importDeclarations.flatMap { it.importedForeignValueDeclarations })
                yieldAll(importDeclarations.flatMap { it.importedClassMembers })
                val importedClassMembers =
                    importDeclarations
                        .asSequence()
                        .flatMap { it.importedClassDeclarations.asSequence() }
                        .flatMap { it.classMembers.asSequence() }
                yieldAll(importedClassMembers)
            }
        }

    override fun getQuickFixes(): Array<LocalQuickFix> {
        val qualifyingName = element.qualifiedIdentifier.moduleName?.name
        return ExportedValuesIndex
            .filesExportingValue(element.project, element.name)
            .mapNotNull { it.module?.name }
            .flatMap { ImportQuickFix.allCombinations(
                it,
                alias = qualifyingName,
                item = ImportedValue(element.name),
            ) }
            .toTypedArray()
    }


    override fun handleElementRename(name: String): PsiElement? {
        val oldName = element.qualifiedIdentifier.identifier
        val newName = PSPsiFactory(element.project).createIdentifier(name)
            ?: return null
        oldName.replace(newName)
        return element
    }
}
