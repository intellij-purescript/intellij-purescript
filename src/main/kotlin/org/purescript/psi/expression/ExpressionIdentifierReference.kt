package org.purescript.psi.expression

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.LocalQuickFixProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.parents
import com.intellij.util.concurrency.Invoker
import com.intellij.util.ui.EDT
import org.purescript.ide.formatting.ImportedValue
import org.purescript.psi.PSPsiFactory
import org.purescript.psi.declaration.imports.ReExportedImportIndex
import org.purescript.psi.declaration.value.ExportedValueDeclNameIndex
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
                            is ValueDecl -> {
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
        val project = element.project
        val scope = GlobalSearchScope.allScope(project)
        val valueDeclarations = ExportedValueDeclNameIndex
            .get(element.name, project, scope)
            .toList()
        val exported = valueDeclarations
            .flatMap { valueDecl ->
                sequenceOf(valueDecl.asImport(), valueDecl.module?.asImport())
            }.filterNotNull()
        val reExports = valueDeclarations
            .mapNotNull { it.module?.name }
            .flatMap { ReExportedImportIndex.get(it, project, scope) }
            .mapNotNull { import -> (import.module?.asImport()) }
            .flatMap {
                sequenceOf(it, it.withItems(ImportedValue(element.name)))
            }
        return (exported)
            .map { it.withAlias(qualifyingName) }
            .map { ImportQuickFix(it) }
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
