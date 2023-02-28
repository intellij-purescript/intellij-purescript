package org.purescript.psi.declaration.imports

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.intellij.psi.util.parentOfType
import com.intellij.psi.util.siblings
import com.intellij.refactoring.safeDelete.NonCodeUsageSearchInfo
import com.intellij.refactoring.safeDelete.SafeDeleteProcessor.findGenericElementUsages
import com.intellij.refactoring.safeDelete.SafeDeleteProcessor.getDefaultInsideDeletedCondition
import com.intellij.refactoring.safeDelete.SafeDeleteProcessorDelegateBase
import com.intellij.usageView.UsageInfo
import org.purescript.PSLanguage
import org.purescript.parser.COMMA
import org.purescript.parser.PSParserDefinition


class ImportSafeDeleteProcessorDelegate : SafeDeleteProcessorDelegateBase() {
    override fun handlesElement(it: PsiElement?): Boolean {
        return it is PSImportedItem
    }
    override fun findUsages(it: PsiElement, toDelete: Array<out PsiElement>, result: MutableList<UsageInfo>)
            : NonCodeUsageSearchInfo {
        findGenericElementUsages(it, result, toDelete)
        val condition = getDefaultInsideDeletedCondition(toDelete)
        return NonCodeUsageSearchInfo(condition, it)
    }

    override fun getElementsToSearch(it: PsiElement, module: Module?, toDelete: MutableCollection<out PsiElement>)
            : MutableCollection<out PsiElement> = mutableSetOf(it)

    override fun getAdditionalElementsToDelete(it: PsiElement, toDelete: Collection<PsiElement?>, askUser: Boolean)
            : Collection<PsiElement>? = when (it) {
        is PSImportedItem -> {
            val import = it.parentOfType<Import>()
            if ((import?.importedItems?.size == 1)) {
                listOf(import)
            } else {
                val definition = PSParserDefinition()
                val comma = it.siblings(true, false)
                    .takeWhile { it.elementType == COMMA ||
                            definition.whitespaceTokens.contains(it.elementType) ||
                            definition.commentTokens.contains(it.elementType)}
                    .toList()
                if (comma.any {it.elementType == COMMA}) comma
                else 
                    comma + it.siblings(false, false)
                        .takeWhile { it.elementType == COMMA ||
                                definition.whitespaceTokens.contains(it.elementType) ||
                                definition.commentTokens.contains(it.elementType)}
                        .toList()
            }
        }

        else -> null
    }

    override fun findConflicts(it: PsiElement, toDelete: Array<PsiElement?>) = null
    override fun preprocessUsages(project: Project, usages: Array<UsageInfo>) = usages
    override fun prepareForDeletion(it: PsiElement) = Unit
    override fun isToSearchInComments(it: PsiElement?): Boolean = false
    override fun setToSearchInComments(it: PsiElement?, enabled: Boolean) = Unit
    override fun isToSearchForTextOccurrences(it: PsiElement?) = false
    override fun setToSearchForTextOccurrences(it: PsiElement?, enabled: Boolean) = Unit
}