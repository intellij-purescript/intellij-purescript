package org.purescript.psi.expression

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.LocalQuickFixProvider
import com.intellij.psi.PsiElementResolveResult.createResults
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.parents
import org.purescript.file.ExportedValuesIndex
import org.purescript.psi.PSLetImpl
import org.purescript.psi.PSValueDeclaration
import org.purescript.psi.`do`.PSDoBlock

class ExpressionIdentifierReference(expressionConstructor: PSExpressionIdentifier) :
    LocalQuickFixProvider,
    PsiReferenceBase.Poly<PSExpressionIdentifier>(
        expressionConstructor,
        expressionConstructor.qualifiedIdentifier.identifier.textRangeInParent,
        false
    ) {

    override fun getVariants(): Array<Any> =
        candidates.toList().toTypedArray()

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val name = element.name
        val declaration = candidates.firstOrNull { it.name == name }
            ?: return emptyArray()
        // TODO We may not want to have a PsiPolyVariantReference reference here
        return if (declaration is PSValueDeclaration) {
            val module = declaration.module
            val allValueDeclarations =
                candidates.filterIsInstance<PSValueDeclaration>()
                    .filter { it.name == name }
                    .filter { it.module == module }
                    .toMutableList()
            createResults(allValueDeclarations)
        } else {
            createResults(declaration)
        }
    }

    private val candidates: Sequence<PsiNamedElement>
        get() {
            val module = element.module ?: return emptySequence()
            val qualifyingName = element.qualifiedIdentifier.moduleName?.name
            if (qualifyingName != null) {
                val importDeclaration = module.importDeclarations
                    .firstOrNull { it.importAlias?.name == qualifyingName }
                    ?: return emptySequence()
                return sequence {
                    yieldAll(importDeclaration.importedValueDeclarations)
                    yieldAll(importDeclaration.importedForeignValueDeclarations)
                    // TODO Support imported class members
                }
            } else {
                return sequence {
                    for (parent in element.parents) {
                        when (parent) {
                            is PSValueDeclaration -> {
                                yieldAll(parent.varBindersInParameters.values)
                                val valueDeclarations = parent
                                    .where
                                    ?.valueDeclarations
                                    ?.asSequence()
                                    ?: sequenceOf()
                                yieldAll(valueDeclarations)
                            }
                            is PSLetImpl ->
                                yieldAll(parent.valueDeclarations.asSequence())
                            is PSDoBlock ->
                                yieldAll(parent.valueDeclarations)
                        }
                    }
                    // TODO Support values defined in the expression
                    yieldAll(module.valueDeclarations.toList())
                    yieldAll(module.foreignValueDeclarations.toList())
                    val localClassMembers = module
                        .classDeclarations
                        .asSequence()
                        .flatMap { it.classMembers.asSequence() }
                    yieldAll(localClassMembers)
                    val importDeclarations =
                        module.importDeclarations.filter { it.importAlias == null }
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
        }

    override fun getQuickFixes(): Array<LocalQuickFix> =
        ExportedValuesIndex
            .filesExportingValue(element.project, element.name)
            .mapNotNull { it.module?.name }
            .map { ImportQuickFix(it) }
            .toTypedArray()
}
