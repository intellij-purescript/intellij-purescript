package org.purescript.psi.declaration

import com.intellij.codeInspection.CommonProblemDescriptor
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.QuickFix
import com.intellij.lang.annotation.ProblemGroup
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.refactoring.BaseRefactoringProcessor
import com.intellij.refactoring.move.MoveHandler
import com.intellij.usageView.BaseUsageViewDescriptor
import com.intellij.usageView.UsageInfo
import org.purescript.ide.formatting.ImportedValue
import org.purescript.psi.PSPsiFactory
import org.purescript.psi.exports.ExportedValue
import org.purescript.psi.expression.ImportQuickFix
import org.purescript.psi.expression.PSExpressionIdentifier
import org.purescript.psi.imports.PSImportedValue
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
        val first = (toMove.signature?: toMove).prevSibling // whitespace
        targetModule.addRange(first, toMove)
        toMove.module?.deleteChildRange(first, toMove)
        val factory = PSPsiFactory(toMove.project)
        targetModule.exports?.let { exportList ->
            val oldNames = exportList.exportedItems.map { 
                it.text
            }
            val newExportList = factory.createExportList(*oldNames.toTypedArray(), toMove.name)
            exportList.replace(newExportList)
        }
        for (usage in usages) {
            when (val toPatch = usage.element) {
                is ExportedValue.Psi -> toPatch.delete()
                is PSImportedValue -> {
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
                    } else {
                        val importDeclaration = factory.createImportDeclaration(
                            targetModule.name,
                            false,
                            toPatch.qualifiedIdentifier.moduleName?.name,
                            listOf(toPatch.name)
                        )!!
                        toPatch.module?.addImportDeclaration(importDeclaration)
                    }
                }
            }
        }
    }

    override fun getCommandName(): String = MoveHandler.getRefactoringName()
}