package org.purescript.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.usageView.UsageInfo

interface UsedElement: PsiElement, PsiNamedElement {
    fun findUsages(): List<UsageInfo> =
        ReferencesSearch
            .search(this, this.useScope)
            .findAll()
            .map(::UsageInfo)
}