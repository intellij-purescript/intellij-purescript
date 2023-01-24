package org.purescript.psi.declaration.value

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.refactoring.RefactoringSettings
import com.intellij.refactoring.safeDelete.NonCodeUsageSearchInfo
import com.intellij.refactoring.safeDelete.SafeDeleteProcessor.findGenericElementUsages
import com.intellij.refactoring.safeDelete.SafeDeleteProcessor.getDefaultInsideDeletedCondition
import com.intellij.refactoring.safeDelete.SafeDeleteProcessorDelegateBase
import com.intellij.usageView.UsageInfo


class ValueDeclSafeDeleteProcessorDelegate :
    SafeDeleteProcessorDelegateBase() {

    override fun handlesElement(it: PsiElement?): Boolean {
        return it is ValueDecl
    }

    override fun findUsages(
        it: PsiElement,
        toDelete: Array<out PsiElement>,
        result: MutableList<in UsageInfo>
    ): NonCodeUsageSearchInfo {
        findGenericElementUsages(it, result, toDelete)
        val condition = getDefaultInsideDeletedCondition(toDelete)
        return NonCodeUsageSearchInfo(condition, it)
    }

    override fun getElementsToSearch(
        it: PsiElement,
        module: Module?,
        toDelete: MutableCollection<out PsiElement>
    ): MutableCollection<out PsiElement> {
        return mutableSetOf(it)
    }

    override fun getAdditionalElementsToDelete(
        it: PsiElement,
        toDelete: Collection<PsiElement?>,
        askUser: Boolean
    ): Collection<PsiElement>? = when (it) {
        is ValueDecl ->
            // TODO also remove export
            arrayListOf(it.signature).filterNotNull() + it.docComments

        else -> null
    }

    override fun findConflicts(it: PsiElement, toDelete: Array<PsiElement?>) =
        null

    override fun preprocessUsages(project: Project, usages: Array<UsageInfo>) =
        usages

    override fun prepareForDeletion(it: PsiElement) = Unit
    override fun isToSearchInComments(it: PsiElement?): Boolean =
        RefactoringSettings.getInstance().SAFE_DELETE_SEARCH_IN_COMMENTS

    override fun setToSearchInComments(it: PsiElement?, enabled: Boolean) {
        RefactoringSettings.getInstance().SAFE_DELETE_SEARCH_IN_COMMENTS =
            enabled
    }

    override fun isToSearchForTextOccurrences(it: PsiElement?) =
        RefactoringSettings.getInstance().SAFE_DELETE_SEARCH_IN_NON_JAVA

    override fun setToSearchForTextOccurrences(
        it: PsiElement?,
        enabled: Boolean
    ) {
        RefactoringSettings.getInstance().SAFE_DELETE_SEARCH_IN_NON_JAVA =
            enabled
    }
}