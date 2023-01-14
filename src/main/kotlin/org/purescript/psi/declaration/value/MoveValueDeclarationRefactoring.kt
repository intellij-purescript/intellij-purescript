package org.purescript.psi.declaration.value

import com.intellij.openapi.components.service
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.refactoring.BaseRefactoringProcessor
import com.intellij.refactoring.move.MoveHandler
import com.intellij.usageView.BaseUsageViewDescriptor
import com.intellij.usageView.UsageInfo
import org.purescript.psi.PSPsiFactory
import org.purescript.psi.declaration.fixity.FixityDeclaration
import org.purescript.psi.exports.ExportedValue
import org.purescript.psi.expression.PSExpressionConstructor
import org.purescript.psi.expression.PSExpressionIdentifier
import org.purescript.psi.expression.PSExpressionOperator
import org.purescript.psi.expression.PSExpressionSymbol
import org.purescript.psi.declaration.imports.PSImportedValue
import org.purescript.psi.module.Module

class MoveValueDeclarationRefactoring(
    private val toMove: PSValueDeclaration,
    private val targetModule: Module.Psi
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
        val first = (toMove.signature ?: toMove)
        targetModule.add(factory.createNewLines(2))
        targetModule.addRange(first, toMove)
        sourceModule?.deleteChildRange(first, toMove)
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
                        val newImport = factory.createImportDeclaration(
                            targetModule.name,
                            false,
                            toPatch.importDeclaration.importAlias?.name,
                            listOf(toPatch.name)
                        )
                        toPatch.module?.addImportDeclaration(newImport)
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
                        val newImport = factory.createImportDeclaration(
                            targetModule.name,
                            false,
                            null,
                            listOf(toPatch.name)
                        )
                        sourceModule?.addImportDeclaration(newImport)
                        importedInSource = true
                    }
                }
            }
        }
        val done = mutableSetOf<Triple<String, String?, String>>()
        for ((element, reference) in atomDependencies) {
            when (element) {
                is PSExpressionIdentifier -> when (reference) {
                    is PSValueDeclaration -> {
                        val moduleName = reference.module?.name ?: continue
                        val alias = element.qualifiedIdentifier.moduleName?.name
                        val name = element.name
                        when (val address = Triple(moduleName, alias, name)) {
                            in done -> continue
                            else -> done.add(address)
                        }
                        val newImport = factory.createImportDeclaration(
                            moduleName,
                            false,
                            alias,
                            listOf(name)
                        )
                        targetModule.addImportDeclaration(newImport)
                    }
                }

                is PSExpressionOperator -> when (reference) {
                    is FixityDeclaration.Psi -> {
                        val moduleName = reference.module.name
                        val alias = element.qualifiedOperator.moduleName?.name
                        val name = element.name
                        when (val address = Triple(moduleName, alias, name)) {
                            in done -> continue
                            else -> done.add(address)
                        }
                        val newImport = factory.createImportDeclaration(
                            moduleName,
                            false,
                            alias,
                            listOf("($name)")
                        )
                        targetModule.addImportDeclaration(newImport)
                    }
                }

                is PSExpressionConstructor -> TODO()
                is PSExpressionSymbol -> TODO()
            }
        }
    }

    override fun getCommandName(): String = MoveHandler.getRefactoringName()
}