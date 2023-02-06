package org.purescript.psi.declaration.value

import com.intellij.openapi.components.service
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.refactoring.BaseRefactoringProcessor
import com.intellij.refactoring.move.MoveHandler
import com.intellij.usageView.BaseUsageViewDescriptor
import com.intellij.usageView.UsageInfo
import org.purescript.ide.formatting.ImportDeclaration
import org.purescript.ide.formatting.ImportedValue
import org.purescript.psi.PSPsiFactory
import org.purescript.psi.declaration.fixity.FixityDeclaration
import org.purescript.psi.declaration.imports.PSImportedValue
import org.purescript.psi.exports.ExportedValue
import org.purescript.psi.expression.*
import org.purescript.psi.module.Module

class MoveValueDeclRefactoring(
    private val toMove: ValueDeclarationGroup,
    private val targetModule: Module
) : BaseRefactoringProcessor(toMove.project) {
    override fun createUsageViewDescriptor(usages: Array<out UsageInfo>) =
        BaseUsageViewDescriptor(toMove)

    public override fun findUsages(): Array<UsageInfo> =
        ReferencesSearch
            .search(toMove, GlobalSearchScope.projectScope(toMove.project))
            .findAll()
            .map(::UsageInfo)
            .toTypedArray()

    override fun performRefactoring(usages: Array<out UsageInfo>) {
        val factory = toMove.project.service<PSPsiFactory>()
        val sourceModule = toMove.module

        // dependencies needs to be imported or moved to targetModule
        val atomDependencies = toMove.expressionAtoms
            .mapNotNull { element ->
                element.getReference()?.resolve()?.let { element to it }
            }.filter { (_, reference) ->
                // dependency to self is fine
                reference != toMove
            }.filter { (_, reference) ->
                // dependency to locals are fine    
                !(reference.containingFile == toMove.containingFile &&
                    toMove.textRange.contains(reference.textRange))
            }
        targetModule.add(factory.createNewLines(2))
        targetModule.addRange(toMove, toMove)
        sourceModule?.deleteChildRange(toMove, toMove)
        targetModule.exports?.let { exportList ->
            val oldNames = exportList.exportedItems.map {
                it.text
            }
            val newExportList =
                factory.createExportList(*oldNames.toTypedArray(), toMove.name)
            exportList.replace(newExportList)
        }
        var importedInSource = false
        for (usage in usages) {
            when (val toPatch = usage.element) {
                is ExportedValue.Psi -> toPatch.delete()
                is PSImportedValue -> {
                    if (toPatch.module != targetModule) {
                        val importDeclaration = ImportDeclaration(
                            targetModule.name,
                            false,
                            setOf(ImportedValue(toPatch.name)),
                            toPatch.importDeclaration.importAlias?.name
                        )
                        toPatch.module?.addImportDeclaration(importDeclaration)
                    }
                    // remove old one
                    val importDeclaration = toPatch.importDeclaration
                    if (importDeclaration.importList?.importedItems?.size == 1) {
                        if (importDeclaration.isHiding) {
                            importDeclaration.importList?.delete()
                        } else {
                            importDeclaration.delete()
                        }
                    } else {
                        toPatch.delete()
                    }
                }

                is PSExpressionIdentifier -> {
                    if (toPatch.module == targetModule) {
                        toPatch.qualifiedIdentifier.moduleName?.delete()
                    } else if (toPatch.module == sourceModule && !importedInSource) {
                        val importDeclaration = ImportDeclaration(
                            targetModule.name,
                            false,
                            setOf(ImportedValue(toPatch.name)),
                        )
                        sourceModule?.addImportDeclaration(importDeclaration)
                        importedInSource = true
                    }
                }
            }
        }
        val done = mutableSetOf<ImportDeclaration>()
        for ((element, reference) in atomDependencies) {
            when (element) {
                is PSExpressionIdentifier -> when (reference) {
                    is ValueDeclarationGroup -> {
                        val importDeclaration = reference.asImport()
                            ?.withAlias(element.qualifierName)
                            ?: continue
                        if (importDeclaration in done) continue
                        else done.add(importDeclaration)
                        targetModule.addImportDeclaration(importDeclaration)
                    }
                }

                is PSExpressionOperator -> when (reference) {
                    is FixityDeclaration -> {
                        val importDeclaration = reference.asImport()
                            ?.withAlias(element.qualifierName)
                            ?: continue
                        if (importDeclaration in done) continue
                        else done.add(importDeclaration)
                        targetModule.addImportDeclaration(importDeclaration)
                    }
                }

                is PSExpressionConstructor -> TODO()
                is PSExpressionSymbol -> TODO()
                is PSArrayLiteral -> TODO()
            }
        }
    }

    override fun getCommandName(): String = MoveHandler.getRefactoringName()
}