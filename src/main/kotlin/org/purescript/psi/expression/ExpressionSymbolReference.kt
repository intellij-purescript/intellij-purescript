package org.purescript.psi.expression

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.LocalQuickFixProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import org.purescript.file.ExportedFixityIndex
import org.purescript.ide.formatting.ImportedOperator
import org.purescript.psi.base.PSPsiElement
import org.purescript.psi.PSPsiFactory
import org.purescript.psi.name.PSModuleName
import org.purescript.psi.name.PSOperatorName

class ExpressionSymbolReference(
    symbol: PSPsiElement, val moduleName: PSModuleName?, val operator: PSOperatorName
) : LocalQuickFixProvider,
    PsiReferenceBase<PSPsiElement>(
        symbol,
        operator.textRangeInParent,
        false
    ) {

    override fun getVariants(): Array<Any> =
        candidates.toList().toTypedArray()

    override fun resolve(): PsiElement? {
        return candidates.firstOrNull { it.name == element.name }
    }

    val candidates
        get() = sequence {
            val module = element.module ?: return@sequence
            yieldAll(module.fixityDeclarations.asSequence())
            yieldAll(module.cache.imports.flatMap { it.importedFixityDeclarations })
        }

    override fun getQuickFixes(): Array<LocalQuickFix> {
        val qualifyingName = moduleName?.name
        return ExportedFixityIndex
            .filesExportingFixity(element.project, operator.name)
            .mapNotNull { it.module?.name }
            .flatMap {
                ImportQuickFix.allCombinations(
                    it,
                    alias = qualifyingName,
                    item = ImportedOperator(element.name!!),
                )
            }
            .toTypedArray()
    }

    override fun handleElementRename(name: String): PsiElement? {
        val newName = PSPsiFactory(element.project).createOperatorName(name)
            ?: return null
        operator.replace(newName)
        return element
    }
}
